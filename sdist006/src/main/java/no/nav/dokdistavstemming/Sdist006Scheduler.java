package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
@EnableScheduling
public class Sdist006Scheduler {

	private final LeaderElectionConsumer leaderElection;
	private final ThreadPoolTaskExecutor poolTaskExecutor;
	private final SendIkkeLesteForsendelserTilSentralPrintService sendIkkeLesteForsendelserTilSentralPrintService;

	public Sdist006Scheduler(ThreadPoolTaskExecutor poolTaskExecutor, LeaderElectionConsumer leaderElection, SendIkkeLesteForsendelserTilSentralPrintService sendIkkeLesteForsendelserTilSentralPrintService) {
		this.leaderElection = leaderElection;
		this.poolTaskExecutor = poolTaskExecutor;
		this.sendIkkeLesteForsendelserTilSentralPrintService = sendIkkeLesteForsendelserTilSentralPrintService;
	}

	@Scheduled(cron = "${sdist006.cron.job}")
	public void runSdist006() {
		if (leaderElection.isLeader()) {
			log.info("Starter sdist006 cron-jobb");
			poolTaskExecutor.execute(sendIkkeLesteForsendelserTilSentralPrintService::doJob);
		}
	}
}
