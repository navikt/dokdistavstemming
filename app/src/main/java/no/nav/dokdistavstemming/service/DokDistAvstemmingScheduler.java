package no.nav.dokdistavstemming.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederKvitteringForsendelse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DokDistAvstemmingScheduler {

	private final HentUekspederKvitteringForsendelse hentUekspederKvitteringForsendelse;
	private final DokDistAvstemmingService dokDistAvstemmingService;

	public DokDistAvstemmingScheduler(HentUekspederKvitteringForsendelse hentUekspederKvitteringForsendelse,
									  DokDistAvstemmingService dokDistAvstemmingService) {
		this.hentUekspederKvitteringForsendelse = hentUekspederKvitteringForsendelse;
		this.dokDistAvstemmingService = dokDistAvstemmingService;
	}


	@Scheduled(cron = "scheduler.cron:0 0 11 * * MON-FRI")
	public void triggerJiraSak() {
		dokDistAvstemmingService.hentUekspederForsendelserService();
	}
}
