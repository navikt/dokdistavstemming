package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Slf4j
@Configuration
@EnableScheduling
public class Sdist004Scheduler implements SchedulingConfigurer {

	private static final int POOL_SIZE = 2;

	private final String sdist004Schedule;
	private final Sdist004BulkOppdaterJournalpostDistInfoService sdist004BulkOppdaterJournalpostDistInfo;
	private final LeaderElectionConsumer leaderElection;
	private final ThreadPoolTaskScheduler taskScheduler;

	public Sdist004Scheduler(Sdist004BulkOppdaterJournalpostDistInfoService sdist004BulkOppdaterJournalpostDistInfo,
							 @Value("${scheduler_sdist004_cron}") String sdist004Schedule, LeaderElectionConsumer leaderElection) {
		this.sdist004BulkOppdaterJournalpostDistInfo = sdist004BulkOppdaterJournalpostDistInfo;
		this.sdist004Schedule = sdist004Schedule;
		this.leaderElection = leaderElection;
		this.taskScheduler = new ThreadPoolTaskScheduler();
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
		if (leaderElection.isLeader()) {
			log.info("starter sdist004 cron-jobben.");
			taskScheduler.setPoolSize(POOL_SIZE);
			taskScheduler.setThreadNamePrefix("sdist004-scheduled-task-pool-");
			taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
			taskScheduler.initialize();
			scheduledTaskRegistrar.setTaskScheduler(taskScheduler);

			scheduledTaskRegistrar.addCronTask(sdist004BulkOppdaterJournalpostDistInfo::oppdaterAvstemOgJournalpostDistInfo, sdist004Schedule);
		}
	}
}
