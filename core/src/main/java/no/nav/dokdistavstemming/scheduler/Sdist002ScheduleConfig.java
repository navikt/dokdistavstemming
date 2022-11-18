package no.nav.dokdistavstemming.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import no.nav.dokdistavstemming.sdist002.serviceimp.Sdist002Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Configuration
@EnableScheduling
@Slf4j
public class Sdist002ScheduleConfig implements SchedulingConfigurer {

	private static final int POOL_SIZE = 4;

	private final String jiraSchedule;
	private final Sdist002Service sdist002Service;
	private final LeaderElectionConsumer leaderElection;
	
	public Sdist002ScheduleConfig(@Value("${sdist002.cron.job}") String jiraSchedule,
								  Sdist002Service sdist002Service, LeaderElectionConsumer leaderElection) {
		this.sdist002Service = sdist002Service;
		this.jiraSchedule = jiraSchedule;
		this.leaderElection = leaderElection;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

		if (leaderElection.isLeader()) {
			ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
			taskScheduler.setPoolSize(POOL_SIZE);
			taskScheduler.setThreadNamePrefix("dokdistavstemming-scheduled-task-pool-");
			taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
			taskScheduler.initialize();
			scheduledTaskRegistrar.setTaskScheduler(taskScheduler);
			scheduledTaskRegistrar.addCronTask(sdist002Service::oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal, jiraSchedule);
		}
	}

}