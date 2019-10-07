package no.nav.dokdistavstemming.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.service.serviceimp.DokDistAvstemmingService;
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
public class DokDistAvstemmingScheduleConfig implements SchedulingConfigurer {

	private final int POOL_SIZE = 10;

	private final DokDistAvstemmingService dokDistAvstemmingService;
	private final String cronSchedule;

	public DokDistAvstemmingScheduleConfig(@Value("${scheduler_interval_cron}") String cronScheduler, DokDistAvstemmingService dokDistAvstemmingService) {
		this.dokDistAvstemmingService = dokDistAvstemmingService;
		this.cronSchedule=cronScheduler;
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
						dokDistAvstemmingService.henteDokDistFil();
					} catch (Exception e) {
						log.error(String.format("createJiraSak feilet til å opprette jira sak med feilmelding=%s", e.getMessage()));

					}
				}, cronSchedule);
	}


}
