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

	@Scheduled(cron = "${sdist006.cron.job}")
	public void runSdist006() {
		if (leaderElection.isLeader()) {
			poolTaskExecutor.execute(sendUlesteForsendelserTilSentralPrintService::sendUlesteForsendelserTilSentralPrint);
		}
	}
}
