package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.service.jira.JiraService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Configuration
@EnableScheduling
public class HentDokDistAvstemmingJobScheduleConfig implements SchedulingConfigurer {

	private JiraService jiraService;
	private DokDistAvstemmingService dokDistAvstemmingService;

	public HentDokDistAvstemmingJobScheduleConfig(JiraService jiraService, DokDistAvstemmingService dokDistAvstemmingService) {
		this.jiraService = jiraService;
		this.dokDistAvstemmingService = dokDistAvstemmingService;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
		scheduledTaskRegistrar.addCronTask(() -> jiraService.createJiraSak(),
				"0 46 12 * * MON-FRI");
	}

}
