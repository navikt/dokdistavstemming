package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
public class Sdist006Scheduler {

	private final LeaderElectionConsumer leaderElection;
	private final ThreadPoolTaskExecutor poolTaskExecutor;
	private final SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService;

	public Sdist006Scheduler(ThreadPoolTaskExecutor poolTaskExecutor, LeaderElectionConsumer leaderElection, SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService) {
		this.leaderElection = leaderElection;
		this.poolTaskExecutor = poolTaskExecutor;
		this.sendUlesteForsendelserTilSentralPrintService = sendUlesteForsendelserTilSentralPrintService;
	}

	//kjør en gang 90s etter oppstart for å få ut logger
	@Scheduled(initialDelay = 1000 * 90, fixedDelay=Long.MAX_VALUE) //cron = "${sdist006.cron.job}")
	public void runSdist006() {
		log.info("runSdist006");
		if (leaderElection.isLeader()) {
			log.info("Starter sdist006 cron-jobb");
			poolTaskExecutor.execute(sendUlesteForsendelserTilSentralPrintService::sendUlesteForsendelserTilSentralPrint);
		}
	}
}
