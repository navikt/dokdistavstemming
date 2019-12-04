package no.nav.dokdistavstemming.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
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
public class AvstemForsendelseScheduleConfig implements SchedulingConfigurer {

	private static final int POOL_SIZE = 10;

	private final String cronSchedule;
	private final JiraService jiraService;


	public AvstemForsendelseScheduleConfig(@Value("${scheduler_interval_cron}") String cronScheduler,
										   JiraService jiraService) {
		this.cronSchedule=cronScheduler;
		this.jiraService = jiraService;

	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

		taskScheduler.setPoolSize(POOL_SIZE);
		taskScheduler.setThreadNamePrefix("dokdistavstemming-scheduled-task-pool-");
		taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		taskScheduler.initialize();

		scheduledTaskRegistrar.setTaskScheduler(taskScheduler);
		scheduledTaskRegistrar.addCronTask(() ->
				{
					try {
						jiraService.oppretteMMAJiraSak();
					} catch (Exception e) {
						log.error(String.format("Feilet til å opprette jira sak med feilmelding=%s", e.getMessage()));

					}
				}, cronSchedule);
	}


}
