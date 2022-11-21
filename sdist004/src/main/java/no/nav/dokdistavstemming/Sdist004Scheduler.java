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
public class Sdist004Scheduler {

	private final BulkOppdaterJournalpostDistInfoService sdist004BulkOppdaterJournalpostDistInfo;
	private final LeaderElectionConsumer leaderElection;
	private final ThreadPoolTaskExecutor poolTaskExecutor;

	public Sdist004Scheduler(BulkOppdaterJournalpostDistInfoService sdist004BulkOppdaterJournalpostDistInfo, ThreadPoolTaskExecutor poolTaskExecutor,
							 LeaderElectionConsumer leaderElection) {
		this.sdist004BulkOppdaterJournalpostDistInfo = sdist004BulkOppdaterJournalpostDistInfo;
		this.leaderElection = leaderElection;
		this.poolTaskExecutor = poolTaskExecutor;
	}

	@Scheduled(cron = "${sdist004.cron.job}")
	public void configureTasks() {
		if (leaderElection.isLeader()) {
			log.info("Starter sdist004 cron-jobb ...");
			poolTaskExecutor.setThreadNamePrefix("sdist004-scheduled-task-pool-");
			poolTaskExecutor.execute(sdist004BulkOppdaterJournalpostDistInfo::oppdaterAvstemOgJournalpostDistInfo);
		}
	}
}
