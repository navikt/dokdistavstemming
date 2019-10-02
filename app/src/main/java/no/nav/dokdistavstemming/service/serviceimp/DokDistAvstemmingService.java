package no.nav.dokdistavstemming.service.serviceimp;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingRequestTo;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvstemmingMapper;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

	public DokDistAvstemmingService(HentUekspederForsendelse hentUekspederForsendelse, CSVProdusere csvProdusere) {
		this.hentUekspederForsendelse = hentUekspederForsendelse;
		this.csvProdusere = csvProdusere;
	}

	public List<DokDistAvstemmingRequestTo> hentUekspederForsendelserService(String distribusjonKanal) {
		Long period = (PRINT.name().equals(distribusjonKanal) || SDP_PRINT.name().equals(distribusjonKanal)) ? ANTALL_DAGER : ANTALL_TIMER;
		List<DokDistAvstemmingRequestTo> dokDistAvstemmingRequestTos = hentUekspederForsendelse.hentUekspederForsendelse(distribusjonKanal, period);
		return dokDistAvstemmingRequestTos.stream()
				.filter(hentUekspederForsendelse -> hentUekspederForsendelse.getDokumenter().size() != 0)
				.collect(Collectors.toList());

	}

	public List<File> henteDokDistFil() throws Exception {
		log.info(String.format("Har mottat kall til å opprette  CSV fil fra dokdistavstemming list"));
		return Arrays.asList(csvProdusere.oppretteCsvFil(dokDistAvstemmingUtenPrintJiraSak()),
				csvProdusere.oppretteCsvFil(dokDistAvstemmingUekspederrKanalPrint()));
	}


	public List<DokDistAvstemmingResponseTo> dokDistAvstemmingUtenPrintJiraSak() {

		DokDistAvstemmingMapper dokDistAvstemmingMapper = new DokDistAvstemmingMapper();

		List<DistribusjonKanalCode> distribusjonKanaler = Arrays.stream(DistribusjonKanalCode.values())
				.filter(distribusjonKanal -> PRINT != distribusjonKanal && SDP_PRINT != distribusjonKanal)
				.distinct()
				.collect(Collectors.toList());

		return distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentUekspederForsendelserService(distribusjonKanal.name()))
				.distinct()
				.flatMap(Collection::stream)
				.filter(hentUekspederForsendelse -> hentUekspederForsendelse != null)
				.map(hentUekspederForsendelse -> dokDistAvstemmingMapper.mapDokDistUtenPrint(hentUekspederForsendelse))
				.collect(Collectors.toList());

	}

	//dokDistAvstemmingKanalPrint

	public List<DokDistAvstemmingResponseTo> dokDistAvstemmingUekspederrKanalPrint() {
		DokDistAvstemmingMapper dokDistAvstemmingMapper = new DokDistAvstemmingMapper();

		List<DistribusjonKanalCode> distribusjonKanaler = Arrays.stream(DistribusjonKanalCode.values())
				.filter(distribusjonKanal -> PRINT.equals(distribusjonKanal))
				.collect(Collectors.toList());
		return distribusjonKanaler.stream()
				.map(distribusjonKanal -> hentUekspederForsendelserService(distribusjonKanal.name()))
				.distinct()
				.flatMap(Collection::stream)
				.filter(hentUekspederForsendelse -> hentUekspederForsendelse != null)
				.map(hentUekspederForsendelse -> dokDistAvstemmingMapper.mapDokDistPrint(hentUekspederForsendelse))
				.collect(Collectors.toList());

	}


}
