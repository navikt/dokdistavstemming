package no.nav.dokdistavstemming.sdist006;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Sdist006Scheduler {

	private final LeaderElectionConsumer leaderElectionConsumer;
	private final SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService;

	public Sdist006Scheduler(LeaderElectionConsumer leaderElectionConsumer,
							 SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService) {
		this.leaderElectionConsumer = leaderElectionConsumer;
		this.sendUlesteForsendelserTilSentralPrintService = sendUlesteForsendelserTilSentralPrintService;
	}

	@Scheduled(cron = "${sdist006.cron.job}")
	public void runSdist006() {
		if (leaderElectionConsumer.isLeader()) {
			log.info("Sdist006 cron-jobb starter");
			sendUlesteForsendelserTilSentralPrintService.sendUlesteForsendelserTilSentralPrint();
			log.info("Sdist006 er ferdig");
		}
	}
}
