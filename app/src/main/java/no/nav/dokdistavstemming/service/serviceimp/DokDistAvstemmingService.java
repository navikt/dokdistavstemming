package no.nav.dokdistavstemming.service.serviceimp;


import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingRequestTo;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvstemmingMapper;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP_PRINT;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class DokDistAvstemmingService {

	private static final Long ANTALL_TIMER = 24L;
	private static final Long ANTALL_DAGER = 144L; // 144 timer er 6 dager
	public static final String DOK_REQUEST_FUNCTIONAL_COUNTER = "dok_request_functional_counter";
	private final HentUekspederForsendelse hentUekspederForsendelse;
	private final CSVProdusere csvProdusere;
	private final MeterRegistry meterRegistry;


	public DokDistAvstemmingService(HentUekspederForsendelse hentUekspederForsendelse, CSVProdusere csvProdusere,
									MeterRegistry meterRegistry) {
		this.hentUekspederForsendelse = hentUekspederForsendelse;
		this.csvProdusere = csvProdusere;
		this.meterRegistry = meterRegistry;
	}

	public List<DokDistAvstemmingRequestTo> hentUekspederForsendelserService(String distribusjonKanal) {
		Long period = (PRINT.name().equals(distribusjonKanal) || SDP_PRINT.name().equals(distribusjonKanal)) ? ANTALL_DAGER : ANTALL_TIMER;
		List<DokDistAvstemmingRequestTo> dokDistAvstemmingRequestTos = hentUekspederForsendelse.hentUekspederForsendelse(distribusjonKanal, period);
		return dokDistAvstemmingRequestTos.stream()
				.filter(uekspederForsendelse -> !uekspederForsendelse.getDokumenter().isEmpty())
				.collect(Collectors.toList());

	}

	public List<File> henteDokDistFil() throws Exception {
		log.info("Har mottat kall til å opprette  CSV fil fra uekspedert forsendelse");

		File csvFilSDPKanal = csvProdusere.oppretteCsvFil(dokDistAvstemmingUtenPrintJiraSak());
		File csvFilPrintKanal = csvProdusere.oppretteCsvFil(dokDistAvstemmingUekspederKanalPrint());
		List<File> produsereCSVFiler = Arrays.asList(csvFilPrintKanal, csvFilSDPKanal);
		return produsereCSVFiler.stream()
				.filter(csvFil -> isFilExistOgNotNull(csvFil))
				.collect(Collectors.toList());
	}


	public List<DokDistAvstemmingResponseTo> dokDistAvstemmingUtenPrintJiraSak() {
		DokDistAvstemmingMapper dokDistAvstemmingMapper = new DokDistAvstemmingMapper();
		List<DistribusjonKanalCode> distribusjonKanaler = Arrays.stream(DistribusjonKanalCode.values())
				.filter(distribusjonKanal -> PRINT != distribusjonKanal)
				.distinct()
				.collect(Collectors.toList());
		Set<DokDistAvstemmingResponseTo> uekspederFrosendelseUtenPrint = distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentUekspederForsendelserService(distribusjonKanal.name()))
				.filter(Objects::nonNull)
				.distinct()
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.map(uekspederForsendelse -> {
					DokDistAvstemmingResponseTo dokDistAvstemming = dokDistAvstemmingMapper.mapDokDistUtenPrint(uekspederForsendelse);
					incrementFunctionalMetrics(dokDistAvstemming.getDistribusjonKanal(),dokDistAvstemming.getDistribusjonDato(),
							dokDistAvstemming.getDistribusjonStatus());
					log.info(String.format("DokDistAvstemming fant uekspedert forsendelse med distribusjonId=%s, arkivKode=%s,distStatus=%s, distribusjonDato=%s,distribusjonKanal=%s", dokDistAvstemming.getDistribusjonId(),
							dokDistAvstemming.getArkivKode(), dokDistAvstemming.getDistribusjonStatus(),dokDistAvstemming.getDistribusjonDato(), dokDistAvstemming.getDistribusjonKanal()));
					return dokDistAvstemming;

				})
				.collect(Collectors.toSet());
		return new ArrayList<>(uekspederFrosendelseUtenPrint);
	}

	//dokDistAvstemmingKanalPrint

	public List<DokDistAvstemmingResponseTo> dokDistAvstemmingUekspederKanalPrint() {
		DokDistAvstemmingMapper dokDistAvstemmingMapper = new DokDistAvstemmingMapper();

		List<DistribusjonKanalCode> distribusjonKanaler = Arrays.stream(DistribusjonKanalCode.values())
				.filter(distribusjonKanal -> PRINT == distribusjonKanal)
				.collect(Collectors.toList());
		Set<DokDistAvstemmingResponseTo> uekspederFrosendelsePrint = distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentUekspederForsendelserService(distribusjonKanal.name()))
				.flatMap(Collection::stream)
				.distinct()
				.filter(Objects::nonNull)
				.map(uekspederForsendelse -> {
					incrementFunctionalMetrics(uekspederForsendelse.getDistribusjonKanal(),uekspederForsendelse.getDistribusjonDato(),
							uekspederForsendelse.getDistribusjonStatus());
					log.info(String.format("DokDistAvstemming fant uekspedert forsendelse med distribusjonId=%s, distStatus=%s,distribusjonDato=%s,distribusjonKanal=%s",
							uekspederForsendelse.getDistribusjonId(), uekspederForsendelse.getDistribusjonStatus(), uekspederForsendelse.getDistribusjonDato(), uekspederForsendelse.getDistribusjonKanal()));
					return dokDistAvstemmingMapper.mapDokDistPrint(uekspederForsendelse);
				})
				.collect(Collectors.toSet());

		return new ArrayList<>(uekspederFrosendelsePrint);
	}




	private void incrementFunctionalMetrics(String distribusjonKanal,String distribusjonDato, String distribusjonStatus) {
		if (distribusjonKanal == null) {
			return;
		}
		meterRegistry.counter(DOK_REQUEST_FUNCTIONAL_COUNTER,
				"distribusjonKanal",distribusjonKanal==null?null:distribusjonKanal,
				"distribusjonDato",distribusjonDato==null?null:distribusjonDato,
				"distribusjonStatus",distribusjonStatus==null?null:distribusjonStatus).increment();

	}

	private boolean isFilExistOgNotNull(File fil) {
		return fil.exists() && fil.length() > 0;
	}

}
