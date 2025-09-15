package no.nav.dokdistavstemming.sdist002;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.consumer.dokdistadmin.DokdistadminRdist001Api;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.map.OppdaterForsendelserAvstemtInfoMapper;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class Sdist002Service {

	private static final String DOK_REQUEST_FUNCTIONAL_COUNTER = "dokdist_antall_delay_kvittering_counter";
	private static final String UKJENT = "Ukjent";

	private final DokdistadminRdist001Api hentForsendelseKvitteringIkkeMottatt;
	private final OppdaterForsendelserAvstemtInfoMapper oppdaterForsendelserMapper;
	private final UekspedertForsendelseMapper uekspedertForsendelseMapper;
	private final CSVProducer csvProducer;
	private final MeterRegistry meterRegistry;
	private final JiraOppgaveService jiraOppgaveService;
	private final DokdistavstemmingProperties dokdistavstemmingProp;

	public Sdist002Service(DokdistadminRdist001Api hentForsendelseKvitteringIkkeMottatt,
						   CSVProducer csvProducer,
						   MeterRegistry meterRegistry,
						   JiraOppgaveService jiraOppgaveService,
						   DokdistavstemmingProperties dokdistavstemmingProp) {
		this.hentForsendelseKvitteringIkkeMottatt = hentForsendelseKvitteringIkkeMottatt;
		this.oppdaterForsendelserMapper = new OppdaterForsendelserAvstemtInfoMapper();
		this.uekspedertForsendelseMapper = new UekspedertForsendelseMapper();
		this.csvProducer = csvProducer;
		this.meterRegistry = meterRegistry;
		this.jiraOppgaveService = jiraOppgaveService;
		this.dokdistavstemmingProp = dokdistavstemmingProp;
	}

	public void oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal() {
		Arrays.stream(DistribusjonKanalCode.values())
				.forEach(distribusjonskanal -> {
					List<UekspedertForsendelseDokument> dokumenter = getForsendelserByDistribusjonKanal(distribusjonskanal);

					if (dokumenter.isEmpty()) {
						return;
					}

					LocalDate eldsteAvstemteDokumentDato = dokumenter.stream()
							.map(UekspedertForsendelseDokument::distribusjonDato)
							// .map(LOCAL_DATE_TIME_FORMAT::parse)
							.map(LocalDate::from)
							.min(Comparator.naturalOrder())
							.orElse(LocalDate.now());

					byte[] csv = csvProducer.oppretteCsv(dokumenter, distribusjonskanal);
					JiraSakResponseTo jiraSakResponseTo = jiraOppgaveService.opprettJirasak(distribusjonskanal, csv, dokumenter.size(), eldsteAvstemteDokumentDato);
					hentForsendelseKvitteringIkkeMottatt.oppdaterForsendelserAvstemtDatoOgReferanse(oppdaterForsendelserMapper.map(dokumenter, jiraSakResponseTo));
				});
	}

	List<UekspedertForsendelseDokument> getForsendelserByDistribusjonKanal(DistribusjonKanalCode distribusjonskanal) {
		HentUekspederteForsendelserResponse response = hentForsendelserKvitteringIkkeMottattService(distribusjonskanal);
		List<HentUekspederteForsendelserResponse.UekspedertForsendelse> uekspederteForsendelser = response.getUekspederteForsendelser();

		log.info("Sdist002 fant {} forsendelser med distribusjonskanal={} som ikke har mottatt kvittering", uekspederteForsendelser.size(), distribusjonskanal);

		return uekspederteForsendelser.stream()
				.filter(forsendelse -> forsendelse != null && forsendelse.getDokumenter() != null)
				.map(uekspedertForsendelseMapper::mapUekspederteForsendelser)
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(UekspedertForsendelseDokument::opprettetDato))
				.peek(avstemForsendelse -> {
					incrementFunctionalMetrics(avstemForsendelse.distribusjonKanal(), avstemForsendelse.distribusjonStatus());
					logInfo(avstemForsendelse);
				})
				.toList();
	}

	private static void logInfo(UekspedertForsendelseDokument avstemForsendelse) {
		log.debug("Sdist002 fant uekspedert forsendelse med forsendelseId={}, dokumentId={}, dokumentStatus={}, opprettetDato={}, distribusjonKanal={}, journalpostId={}",
				avstemForsendelse.forsendelseId(),
				avstemForsendelse.dokumentId(),
				avstemForsendelse.dokumentStatus(),
				avstemForsendelse.opprettetDato(),
				avstemForsendelse.distribusjonKanal(),
				avstemForsendelse.journalpostId());
	}

	HentUekspederteForsendelserResponse hentForsendelserKvitteringIkkeMottattService(DistribusjonKanalCode distribusjonKanal) {
		// Defaulter til samme verdi for andre distribusjonskanaler enn PRINT og E_HANDEL
		int antallTimer = switch (distribusjonKanal) {
			case PRINT -> dokdistavstemmingProp.getSdist002().getDelayTimePrint();
			case E_HANDEL -> dokdistavstemmingProp.getSdist002().getDelayTimeEhandel();
			default -> dokdistavstemmingProp.getSdist002().getDelayTimeSDP();
		};

		return hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(distribusjonKanal.name(), antallTimer);
	}

	private void incrementFunctionalMetrics(String distribusjonKanal, String dokumentStatus) {
		meterRegistry.counter(DOK_REQUEST_FUNCTIONAL_COUNTER,
				"distribusjonKanal", distribusjonKanal == null ? UKJENT : distribusjonKanal,
				"dokumentStatus", dokumentStatus == null ? UKJENT : dokumentStatus).increment();
	}
}
