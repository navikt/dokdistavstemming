package no.nav.dokdistavstemming.service.serviceimp;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
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

	private static final Long ANTALL_TIMER = 24L;
	private static final Long ANTALL_DAGER = 144L; // 144 timer er 6 dager
	public static final String DOK_REQUEST_FUNCTIONAL_COUNTER = "dok_request_functional_counter";
	private static final String UKJENT ="Ukjent";
	private final HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt;
	private final CSVProdusere csvProdusere;
	private final MeterRegistry meterRegistry;

	public AvstemForsendelseService(HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt, CSVProdusere csvProdusere,
									MeterRegistry meterRegistry) {
		this.hentForsendelseKvitteringIkkeMottatt = hentForsendelseKvitteringIkkeMottatt;
		this.csvProdusere = csvProdusere;
		this.meterRegistry = meterRegistry;
	}

	public List<AvstemForsendelseRequestTo> hentForsendelserKvitteringIkkeMottattService(String distribusjonKanal) {
		Long period = (PRINT.name().equals(distribusjonKanal) || SDP_PRINT.name().equals(distribusjonKanal)) ? ANTALL_DAGER : ANTALL_TIMER;
		List<AvstemForsendelseRequestTo> avstemForsendelseRequestTos = hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(distribusjonKanal, period);
		return avstemForsendelseRequestTos.stream()
				.filter(avstemForsendelse -> !avstemForsendelse.getDokumenter().isEmpty())
				.collect(Collectors.toList());
	}

	public List<File> henteDokDistFil() throws Exception {
		log.info("Har mottat kall til å opprette  CSV fil fra forsendelser som mottatt ikke kvittering");

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
		Set<AvstemForsendelseResponseTo> uekspederFrosendelseUtenPrint = distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentForsendelserKvitteringIkkeMottattService(distribusjonKanal.name()))
				.filter(Objects::nonNull)
				.distinct()
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.map(uekspederForsendelse -> {
					AvstemForsendelseResponseTo dokDistAvstemming = avstemForsendelseMapper.mapDokDistUtenPrint(uekspederForsendelse);
					incrementFunctionalMetrics(dokDistAvstemming.getDistribusjonKanal(),dokDistAvstemming.getDistribusjonDato(),
							dokDistAvstemming.getDistribusjonStatus());
					log.info(String.format("DokDistAvstemming fant uekspedert forsendelse med distribusjonId=%s, arkivKode=%s,distStatus=%s, distribusjonDato=%s,distribusjonKanal=%s", dokDistAvstemming.getForsendelseId(),
							dokDistAvstemming.getJournalpostId(), dokDistAvstemming.getDistribusjonStatus(),dokDistAvstemming.getDistribusjonDato(), dokDistAvstemming.getDistribusjonKanal()));

					Timer.builder("måler_forsinkelser").description("duration mellom")
							.tag("kanal",dokDistAvstemming.getDistribusjonKanal())
							.tags("status",dokDistAvstemming.getDistribusjonStatus())
							.register(meterRegistry)
							.record(System.currentTimeMillis()-start, TimeUnit.MILLISECONDS);
					return dokDistAvstemming;

				})
				.collect(Collectors.toSet());

		return new ArrayList<>(uekspederFrosendelseUtenPrint);
	}

	//dokDistAvstemmingKanalPrint

	public List<AvstemForsendelseResponseTo> avstemmForsendelseDistKanalPrint() {
		AvstemForsendelseMapper avstemForsendelseMapper = new AvstemForsendelseMapper();

		List<DistribusjonKanalCode> distribusjonKanaler = Arrays.stream(DistribusjonKanalCode.values())
				.filter(distribusjonKanal -> PRINT == distribusjonKanal)
				.collect(Collectors.toList());
		Set<AvstemForsendelseResponseTo> uekspederFrosendelsePrint = distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentForsendelserKvitteringIkkeMottattService(distribusjonKanal.name()))
				.flatMap(Collection::stream)
				.distinct()
				.filter(Objects::nonNull)
				.map(uekspederForsendelse -> {
					incrementFunctionalMetrics(uekspederForsendelse.getDistribusjonKanal(),uekspederForsendelse.getDistribusjonDato(),
							uekspederForsendelse.getDistribusjonStatus());
					log.info(String.format("DokDistAvstemming har fant uekspedert forsendelse med distribusjonId=%s, distStatus=%s,distribusjonDato=%s,distribusjonKanal=%s",
							uekspederForsendelse.getDistribusjonId(), uekspederForsendelse.getDistribusjonStatus(), uekspederForsendelse.getDistribusjonDato(), uekspederForsendelse.getDistribusjonKanal()));
					return avstemForsendelseMapper.mapDokDistPrint(uekspederForsendelse);
				})
				.collect(Collectors.toSet());

		return new ArrayList<>(uekspederFrosendelsePrint);
	}


	private void incrementFunctionalMetrics(String distribusjonKanal,String distribusjonDato, String distribusjonStatus) {
		meterRegistry.counter(DOK_REQUEST_FUNCTIONAL_COUNTER,
				"distribusjonKanal",distribusjonKanal==null?UKJENT:distribusjonKanal,
				"distribusjonDato",distribusjonDato==null?UKJENT:distribusjonDato,
				"distribusjonStatus",distribusjonStatus==null?UKJENT:distribusjonStatus).increment();
	}

	private boolean isFilExistOgNotNull(File fil) {
		return fil.exists() && fil.length() > 0;
	}

}
