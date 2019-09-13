package no.nav.dokdistavstemming.service;


import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingForsendelse;
import org.springframework.stereotype.Component;

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
	private final HentUekspederForsendelse hentUekspederKvitteringForsendelse;

	public DokDistAvstemmingService(HentUekspederForsendelse hentUekspederKvitteringForsendelse) {
		this.hentUekspederKvitteringForsendelse = hentUekspederKvitteringForsendelse;
	}

	public List<DokDistAvstemmingForsendelse> hentUekspederForsendelserService(DistribusjonKanalCode distribusjonKanalCode) {
		Long period = (PRINT.equals(distribusjonKanalCode) || SDP_PRINT.equals(distribusjonKanalCode)) ? ANTALL_DAGER:ANTALL_TIMER;
		return hentUekspederKvitteringForsendelse.hentUekspederForsendelse(distribusjonKanalCode.name(), period);
	}

	// all those should create a task
	public List<DokDistAvstemmingForsendelse> dokDistAvstemmingUtenPrintJiraSak() {
		List<DokDistAvstemmingForsendelse> dokDistAvstemmingForsendelses =
				Arrays.stream(DistribusjonKanalCode.values())
						.filter(new Predicate<DistribusjonKanalCode>() {
							@Override
							public boolean test(DistribusjonKanalCode distribusjonKanal) {
								return !distribusjonKanal.equals(PRINT) || !distribusjonKanal.equals(SDP_PRINT);
							}
						})
						.map(this::hentUekspederForsendelserService)
						.distinct()
						.filter(new Predicate<List<DokDistAvstemmingForsendelse>>() {
							@Override
							public boolean test(List<DokDistAvstemmingForsendelse> dokDistList) {
								return dokDistList != null && !dokDistList.isEmpty();
							}
						})
						.flatMap(Collection::stream)
						.collect(Collectors.toList());

		return dokDistAvstemmingForsendelses;
	}

	//print og sdp_print samme sak


	public List<DokDistAvstemmingForsendelse> dokDistAvstemmingPrintJiraSak() {
		List<DokDistAvstemmingForsendelse> dokDistAvstemmingForsendelses =
				Arrays.stream(DistribusjonKanalCode.values())
						.filter(new Predicate<DistribusjonKanalCode>() {
							@Override
							public boolean test(DistribusjonKanalCode distribusjonKanal) {
								return distribusjonKanal.equals(PRINT) || distribusjonKanal.equals(SDP_PRINT);
							}
						})
						.map(this::hentUekspederForsendelserService)
						.distinct()
						.filter(new Predicate<List<DokDistAvstemmingForsendelse>>() {
							@Override
							public boolean test(List<DokDistAvstemmingForsendelse> dokDistList) {
								return dokDistList != null && !dokDistList.isEmpty();
							}
						})
						.flatMap(Collection::stream)
						.collect(Collectors.toList());

		return dokDistAvstemmingForsendelses;
	}


}
