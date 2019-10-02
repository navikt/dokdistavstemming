package no.nav.dokdistavstemming.service.serviceimp;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;
import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvStemmingResponseToMapper;
import no.nav.dokdistavstemming.domain.map.MapperDokDist;
import no.nav.dokdistavstemming.domain.to.DokDistAvstemmingUtenPrintTo;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
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

	public List<DokDistAvStemmingResponseTo> hentUekspederForsendelserService(DistribusjonKanalCode distribusjonKanalCode) {
		Long period = (PRINT.equals(distribusjonKanalCode) || SDP_PRINT.equals(distribusjonKanalCode)) ? ANTALL_DAGER : ANTALL_TIMER;
		DokDistAvStemmingResponseToMapper dokDistAvStemmingResponseToMapper = new DokDistAvStemmingResponseToMapper();
		List<HentUekspederForsendelseResponseTo> hentUekspederForsendelseList = hentUekspederForsendelse.hentUekspederForsendelse(distribusjonKanalCode.name(), period);


		return hentUekspederForsendelseList.parallelStream()
				.filter(hentUekspederForsendelse -> !hentUekspederForsendelse.equals(null) &&
						!hentUekspederForsendelse.getDokumenter().equals(null))
				.map(dokDistAvStemmingResponseToMapper::map)
				.collect(Collectors.toList());

	}

	public List<HentUekspederForsendelseResponseTo> hentUekspederForsendelserService1(String distribusjonKanal) {
		Long period = (PRINT.name().equals(distribusjonKanal) || SDP_PRINT.name().equals(distribusjonKanal)) ? ANTALL_DAGER : ANTALL_TIMER;
		List<HentUekspederForsendelseResponseTo> hentUekspederForsendelseResponseTos = hentUekspederForsendelse.hentUekspederForsendelse(distribusjonKanal, period);

		return hentUekspederForsendelseResponseTos.stream()
				.filter(hentUekspederForsendelse -> hentUekspederForsendelse.getDokumenter().size() != 0)
				.collect(Collectors.toList());

	}


	public List<File> henteDokDistFil() throws Exception {
		log.info(String.format("Har mottat kall til å opprette  CSV fil fra dokdistavstemming list"));
		return Arrays.asList(csvProdusere.oppretteCsvFil(dokDistAvstemmingUtenPrint()),
				csvProdusere.oppretteCsvFil(dokDistAvstemmingKanalPrint()));
	}


	public List<DokDistAvstemmingUtenPrintTo> dokDistAvstemmingUtenPrint(){
		List<String> distribusjonKanaler = Arrays.asList("SDP","E_HANDEL","DITTNAV","TRYGDERETTEN");
		MapperDokDist mapperDokDist = new MapperDokDist();
		return distribusjonKanaler.stream()
				.map(distribusjonKanal-> hentUekspederForsendelserService1(distribusjonKanal))
				.distinct()
				.flatMap(Collection::stream)
				.filter(hentUekspederForsendelse -> hentUekspederForsendelse!=null)
				.map(hentUekspederForsendelse ->mapperDokDist.mapDokDistUtenPrint(hentUekspederForsendelse))
				.collect(Collectors.toList());

	}



	public List<DokDistAvstemmingUtenPrintTo> dokDistAvstemmingKanalPrint(){
		List<String> distribusjonKanaler = Arrays.asList("PRINT");
		MapperDokDist mapperDokDist = new MapperDokDist();
		return distribusjonKanaler.stream()
				.map(distribusjonKanal-> hentUekspederForsendelserService1(distribusjonKanal))
				.distinct()
				.flatMap(Collection::stream)
				.filter(hentUekspederForsendelse -> hentUekspederForsendelse!=null)
				.map(hentUekspederForsendelse ->mapperDokDist.mapDokDistPrint(hentUekspederForsendelse))
				.collect(Collectors.toList());

	}








	public List<DokDistAvStemmingResponseTo> dokDistAvstemmingUtenPrintJiraSak() {

		List<DokDistAvStemmingResponseTo> hentUekspederForsendelsResponseTos =
				Arrays.stream(DistribusjonKanalCode.values())
						.filter(distribusjonKanal -> !distribusjonKanal.equals(PRINT) && !distribusjonKanal.equals(SDP_PRINT))
						.map(this::hentUekspederForsendelserService)
						.distinct()
						.filter(new Predicate<List<DokDistAvStemmingResponseTo>>() {
							@Override
							public boolean test(List<DokDistAvStemmingResponseTo> dokDistList) {
								return dokDistList != null && !dokDistList.isEmpty();
							}
						})
						.flatMap(Collection::stream)
						.collect(Collectors.toList());

		return hentUekspederForsendelsResponseTos;
	}

	//print og sdp_print samme sak

	public List<DokDistAvStemmingResponseTo> dokDistAvstemmingUekspederrKanalPrint() {
		List<DokDistAvStemmingResponseTo> hentUekspederForsendelsResponseTos =
				Arrays.stream(DistribusjonKanalCode.values())
						.filter(new Predicate<DistribusjonKanalCode>() {
							@Override
							public boolean test(DistribusjonKanalCode distribusjonKanal) {
								return distribusjonKanal.equals(PRINT) || distribusjonKanal.equals(SDP_PRINT);
							}
						})
						.map(this::hentUekspederForsendelserService)
						.distinct()
						.filter(new Predicate<List<DokDistAvStemmingResponseTo>>() {
							@Override
							public boolean test(List<DokDistAvStemmingResponseTo> dokDistList) {
								return dokDistList != null && !dokDistList.isEmpty();
							}
						})
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
		return hentUekspederForsendelsResponseTos;
	}


}
