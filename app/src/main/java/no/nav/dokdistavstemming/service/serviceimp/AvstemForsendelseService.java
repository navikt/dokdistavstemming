package no.nav.dokdistavstemming.service.serviceimp;


import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP_PRINT;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class AvstemForsendelseService {

	private static final String DOK_REQUEST_FUNCTIONAL_COUNTER = "dok_request_functional_counter";
	private static final Long ANTALL_TIMER = 24L;
	private static final Long ANTALL_DAGER = 120L; // 144 timer er 6 dager
	private static final String UKJENT = "Ukjent";
	private final HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt;
	private final CSVProdusere csvProdusere;
	private final MeterRegistry meterRegistry;

	public AvstemForsendelseService(HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt, CSVProdusere csvProdusere,
									MeterRegistry meterRegistry) {
		this.hentForsendelseKvitteringIkkeMottatt = hentForsendelseKvitteringIkkeMottatt;
		this.csvProdusere = csvProdusere;
		this.meterRegistry = meterRegistry;
	}

	public Set<AvstemForsendelseRequestTo> hentForsendelserKvitteringIkkeMottattService(String distribusjonKanal) {
		Long period = (PRINT.name().equals(distribusjonKanal) || SDP_PRINT.name().equals(distribusjonKanal)) ? ANTALL_DAGER : ANTALL_TIMER;
		List<AvstemForsendelseRequestTo> avstemForsendelseRequestTos = hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(distribusjonKanal, period);
		return avstemForsendelseRequestTos.stream()
				.filter(avstemForsendelse -> !avstemForsendelse.getDokumenter().isEmpty())
				.collect(Collectors.toSet());
	}

	public List<File> henteDokDistFil() throws Exception {
		log.info("Har mottat kall til å opprette  CSV fil fra forsendelser som har ikke mottatt kvittering");

		File csvFilSDPKanal = csvProdusere.oppretteCsvFil(avstemmForsendelseDistKanalUtenPrint());
		File csvFilPrintKanal = csvProdusere.oppretteCsvFil(avstemmForsendelseDistKanalPrint());
		List<File> produsereCSVFiler = Arrays.asList(csvFilPrintKanal, csvFilSDPKanal);
		return produsereCSVFiler.stream()
				.filter(this::isFilExistOgNotNull)
				.collect(Collectors.toList());
	}


	public List<AvstemForsendelseResponseTo> avstemmForsendelseDistKanalUtenPrint() {
		AvstemForsendelseMapper avstemForsendelseMapper = new AvstemForsendelseMapper();
		long start = System.currentTimeMillis();

		List<DistribusjonKanalCode> distribusjonKanaler = Arrays.stream(DistribusjonKanalCode.values())
				.filter(distribusjonKanal -> PRINT != distribusjonKanal)
				.distinct()
				.collect(Collectors.toList());
		return distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentForsendelserKvitteringIkkeMottattService(distribusjonKanal.name()))
				.filter(Objects::nonNull)
				.distinct()
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.map(uekspederForsendelse -> {
					AvstemForsendelseResponseTo avstemForsendelse = avstemForsendelseMapper.mapDokDistUtenPrint(uekspederForsendelse);
					incrementFunctionalMetrics(avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getOpprettetDato(), avstemForsendelse.getDistribusjonStatus(), avstemForsendelse.getCountDokument());
					log.info(String.format("DokDistAvstemming har fant forsendelser som kvittering ikke mottatt med forsendelseId=%s, distribusjonStatus=%s,opprettetDato=%s" +
									",distribusjonKanal=%s,antallDokInfo=%s", avstemForsendelse.getForsendelseId(), avstemForsendelse.getDistribusjonStatus(), avstemForsendelse.getOpprettetDato(),
							avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getCountDokument()));
					meterRegistry.timer("måler_forsinkelser",
							"kanal", avstemForsendelse.getDistribusjonKanal(),
							"status", avstemForsendelse.getDistribusjonStatus())
							.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
					return avstemForsendelse;

				})
				.collect(Collectors.toList());
	}


	public List<AvstemForsendelseResponseTo> avstemmForsendelseDistKanalPrint() {
		AvstemForsendelseMapper avstemForsendelseMapper = new AvstemForsendelseMapper();

		List<DistribusjonKanalCode> distribusjonKanaler = Arrays.stream(DistribusjonKanalCode.values())
				.filter(distribusjonKanal -> PRINT == distribusjonKanal)
				.collect(Collectors.toList());
		return distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentForsendelserKvitteringIkkeMottattService(distribusjonKanal.name()))
				.flatMap(Collection::stream)
				.distinct()
				.filter(Objects::nonNull)
				.map(uekspederForsendelse -> {

					AvstemForsendelseResponseTo avstemForsendelse = avstemForsendelseMapper.mapDokDistPrint(uekspederForsendelse);
					incrementFunctionalMetrics(avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getOpprettetDato(), avstemForsendelse.getDistribusjonStatus(), avstemForsendelse.getCountDokument());
					log.info(String.format("DokDistAvstemming har fant forsendelser som kvittering ikke mottatt med forsendelseId=%s, distribusjonStatus=%s,opprettetDato=%s" +
									",distribusjonKanal=%s,antallDokInfo=%s", avstemForsendelse.getForsendelseId(), avstemForsendelse.getDistribusjonStatus(), avstemForsendelse.getOpprettetDato(),
							avstemForsendelse.getDistribusjonKanal(), avstemForsendelse.getCountDokument()));
					return avstemForsendelse;
				})
				.collect(Collectors.toList());

	}


	private void incrementFunctionalMetrics(String distribusjonKanal, String opprettetDato,
											String distribusjonStatus, Long antallDokInfoId) {
		meterRegistry.counter(DOK_REQUEST_FUNCTIONAL_COUNTER,
				"distribusjonKanal", distribusjonKanal == null ? UKJENT : distribusjonKanal,
				"opprettetDato", opprettetDato == null ? UKJENT : opprettetDato,
				"distribusjonStatus", distribusjonStatus == null ? UKJENT : distribusjonStatus,
				"antallDokInfoId", antallDokInfoId == null ? UKJENT : String.valueOf(antallDokInfoId)).increment();
	}

	private boolean isFilExistOgNotNull(File fil) {
		return fil.exists() && fil.length() > 0;
	}

}
