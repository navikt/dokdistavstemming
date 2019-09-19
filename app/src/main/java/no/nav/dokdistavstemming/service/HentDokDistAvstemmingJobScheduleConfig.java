package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.service.jira.JiraService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class HentDokDistAvstemmingJobScheduleConfig implements SchedulingConfigurer {

	private JiraService jiraService;

	public HentDokDistAvstemmingJobScheduleConfig( JiraService jiraService) {
		this.jiraService = jiraService;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
		scheduledTaskRegistrar.addCronTask(() ->jiraService.createSakJira(), "0 40 20 * * MON-FRI");
	}

}
