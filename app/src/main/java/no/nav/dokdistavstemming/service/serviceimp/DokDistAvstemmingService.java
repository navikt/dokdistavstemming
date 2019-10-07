package no.nav.dokdistavstemming.service.serviceimp;


import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingRequestTo;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvstemmingMapper;
import no.nav.dokdistavstemming.metrics.MetricUtils;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.utils.ConverterUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP_PRINT;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class DokDistAvstemmingService {

	private static final Long ANTALL_TIMER = 6L;
	private static final Long ANTALL_DAGER = 120L; // 120 timer er 5 dager
	private final HentUekspederForsendelse hentUekspederForsendelse;
	private final CSVProdusere csvProdusere;

	private final Counter uekspederCounterKanalPRINT;
	private final Counter uekspederCounterKanalSDP;
	private final Counter uekspederCounterKanalSPD_PRINT;
	private final Counter uekspederCounterKanalE_HANDEL;
	private final Counter uekspederCounterKanalPRINT_DITTNAV;
	private final Counter uekspederCounterKanalDITTNAV;
	private final Counter uekspederCounterKanalTRYGDERETTEN;


	public DokDistAvstemmingService(HentUekspederForsendelse hentUekspederForsendelse, CSVProdusere csvProdusere,
									MetricUtils metricUtils) {
		this.hentUekspederForsendelse = hentUekspederForsendelse;
		this.csvProdusere = csvProdusere;
		this.uekspederCounterKanalPRINT = metricUtils.initFunctionalCounter("Uekspeder Frosendelse", "PRINT");
		this.uekspederCounterKanalSDP = metricUtils.initFunctionalCounter("Uekspeder Frosendelse", "SDP");
		this.uekspederCounterKanalSPD_PRINT = metricUtils.initFunctionalCounter("Uekspeder Frosendelse", "SPD_PRINT");
		this.uekspederCounterKanalE_HANDEL = metricUtils.initFunctionalCounter("Uekspeder Frosendelse", "E_HANDEL");
		this.uekspederCounterKanalPRINT_DITTNAV = metricUtils.initFunctionalCounter("Uekspeder Frosendelse", "PRINT_DITTNAV");
		this.uekspederCounterKanalDITTNAV = metricUtils.initFunctionalCounter("Uekspeder Frosendelse", "DITTNAV");
		this.uekspederCounterKanalTRYGDERETTEN = metricUtils.initFunctionalCounter("Uekspeder Frosendelse", "TRYGDERETTEN");
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
		File csvFilPrintKanal =csvProdusere.oppretteCsvFil(dokDistAvstemmingUekspederrKanalPrint());

		List<File> produsereCSVFiler = Arrays.asList(csvFilPrintKanal,csvFilSDPKanal);

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

		return distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentUekspederForsendelserService(distribusjonKanal.name()))
				.filter(Objects::nonNull)
				.distinct()
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.map(uekspederForsendelse -> {
					DokDistAvstemmingResponseTo dokDistAvstemming = dokDistAvstemmingMapper.mapDokDistUtenPrint(uekspederForsendelse);
					incrementFunctionalMetrics(ConverterUtils.stringToEnum(dokDistAvstemming.getDistribusjonKanal(), DistribusjonKanalCode.class));
					log.info(String.format("Fant uekspedert forsendelse med  distribusjonId=%s, arkivKode=%s distribusjonKanalCode=%s", dokDistAvstemming.getDistribusjonId(),
							dokDistAvstemming.getArkivKode(), dokDistAvstemming.getDistribusjonKanal()));
					return dokDistAvstemming;

				})
				.collect(Collectors.toList());

	}

	//dokDistAvstemmingKanalPrint

	public List<DokDistAvstemmingResponseTo> dokDistAvstemmingUekspederrKanalPrint() {
		DokDistAvstemmingMapper dokDistAvstemmingMapper = new DokDistAvstemmingMapper();

		List<DistribusjonKanalCode> distribusjonKanaler = Arrays.stream(DistribusjonKanalCode.values())
				.filter(distribusjonKanal -> PRINT == distribusjonKanal)
				.collect(Collectors.toList());
		return distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentUekspederForsendelserService(distribusjonKanal.name()))
				.distinct()
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.map(uekspederForsendelse -> {
					incrementFunctionalMetrics(PRINT);
					log.info(String.format("Fant uekspedert forsendelse, distribusjonId=%s distribusjonKanalCode=%s", uekspederForsendelse.getDistribusjonId(), uekspederForsendelse.getDistribusjonKanal()));
					return dokDistAvstemmingMapper.mapDokDistPrint(uekspederForsendelse);
				})
				.collect(Collectors.toList());

	}


	private void incrementFunctionalMetrics(DistribusjonKanalCode distribusjonKanal) {
		if (distribusjonKanal == null) {
			return;
		}
		switch (distribusjonKanal) {
			case PRINT:
				uekspederCounterKanalPRINT.increment();
				break;
			case SDP:
				uekspederCounterKanalSDP.increment();
				break;
			case SDP_PRINT:
				uekspederCounterKanalSPD_PRINT.increment();
				break;
			case E_HANDEL:
				uekspederCounterKanalE_HANDEL.increment();
				break;
			case PRINT_DITTNAV:
				uekspederCounterKanalPRINT_DITTNAV.increment();
				break;
			case DITTNAV:
				uekspederCounterKanalDITTNAV.increment();
				break;
			case TRYGDERETTEN:
				uekspederCounterKanalTRYGDERETTEN.increment();
				break;
		}
	}

	private boolean isFilExistOgNotNull(File fil){

		return fil.exists() && fil.length()>0;
	}

}
