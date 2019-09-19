package no.nav.dokdistavstemming.service;


import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;
import no.nav.dokdistavstemming.scheduler.LeaderElection;
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
	private final LeaderElection leaderElection;

	public DokDistAvstemmingService(HentUekspederForsendelse hentUekspederForsendelse, CSVProdusere csvProdusere, LeaderElection leaderElection) {
		this.hentUekspederForsendelse = hentUekspederForsendelse;
		this.csvProdusere = csvProdusere;
		this.leaderElection = leaderElection;
	}

	public List<HentUekspederForsendelseResponseTo> hentUekspederForsendelserService(DistribusjonKanalCode distribusjonKanalCode) {
		Long period = (PRINT.equals(distribusjonKanalCode) || SDP_PRINT.equals(distribusjonKanalCode)) ? ANTALL_DAGER : ANTALL_TIMER;
		return hentUekspederForsendelse.hentUekspederForsendelse(distribusjonKanalCode.name(), period);
	}


	@Scheduled(cron = "0 33 20 * * MON-FRI")
	public void scheduleDokDistAvstemming() throws IOException {
		if (leaderElection.isLeader()) {
			//csvProdusere.oppretteCsvObject(dokDistAvstemmingPrintJiraSak());
			dokDistAvstemmingPrintJiraSak();
		}

	}

	// all those should create a task
	public List<HentUekspederForsendelseResponseTo> dokDistAvstemmingUtenPrintJiraSak() {
		List<HentUekspederForsendelseResponseTo> hentUekspederForsendelsResponseTos =
				Arrays.stream(DistribusjonKanalCode.values())
						.filter(new Predicate<DistribusjonKanalCode>() {
							@Override
							public boolean test(DistribusjonKanalCode distribusjonKanal) {
								return !distribusjonKanal.equals(PRINT) || !distribusjonKanal.equals(SDP_PRINT);
							}
						})
						.map(this::hentUekspederForsendelserService)
						.distinct()
						.filter(new Predicate<List<HentUekspederForsendelseResponseTo>>() {
							@Override
							public boolean test(List<HentUekspederForsendelseResponseTo> dokDistList) {
								return dokDistList != null && !dokDistList.isEmpty();
							}
						})
						.flatMap(Collection::stream)
						.collect(Collectors.toList());

		return hentUekspederForsendelsResponseTos;
	}

	//print og sdp_print samme sak


	public List<HentUekspederForsendelseResponseTo> dokDistAvstemmingPrintJiraSak() {
		List<HentUekspederForsendelseResponseTo> hentUekspederForsendelsResponseTos =
				Arrays.stream(DistribusjonKanalCode.values())
						.filter(new Predicate<DistribusjonKanalCode>() {
							@Override
							public boolean test(DistribusjonKanalCode distribusjonKanal) {
								return distribusjonKanal.equals(PRINT) || distribusjonKanal.equals(SDP_PRINT);
							}
						})
						.map(this::hentUekspederForsendelserService)
						.distinct()
						.filter(new Predicate<List<HentUekspederForsendelseResponseTo>>() {
							@Override
							public boolean test(List<HentUekspederForsendelseResponseTo> dokDistList) {
								return dokDistList != null && !dokDistList.isEmpty();
							}
						})
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
		return hentUekspederForsendelsResponseTos;
	}


}
