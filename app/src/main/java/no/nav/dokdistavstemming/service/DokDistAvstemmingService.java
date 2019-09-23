package no.nav.dokdistavstemming.service;


import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;
import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvStemmingResponseToMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

		List<HentUekspederForsendelseResponseTo> hentUekspederForsendelseResponseTos = hentUekspederForsendelse.hentUekspederForsendelse(distribusjonKanalCode.name(), period);

		return hentUekspederForsendelseResponseTos.stream()
				.map(dokDistAvStemmingResponseToMapper::map)
				.collect(Collectors.toList());

	}


	// all those should create a task
	public List<DokDistAvStemmingResponseTo> dokDistAvstemmingUtenPrintJiraSak() {
		List<DokDistAvStemmingResponseTo> hentUekspederForsendelsResponseTos =
				Arrays.stream(DistribusjonKanalCode.values())
						.filter(new Predicate<DistribusjonKanalCode>() {
							@Override
							public boolean test(DistribusjonKanalCode distribusjonKanal) {
								return !distribusjonKanal.equals(PRINT) || !distribusjonKanal.equals(SDP_PRINT);
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
