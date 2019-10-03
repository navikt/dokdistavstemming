package no.nav.dokdistavstemming.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.service.serviceimp.DokDistAvstemmingService;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Configuration
@EnableScheduling
@Slf4j
public class HentDokDistAvstemmingJobScheduleConfig implements SchedulingConfigurer {

	private final DokDistAvstemmingService dokDistAvstemmingService;
	private final JiraService jiraService;

	public HentDokDistAvstemmingJobScheduleConfig(DokDistAvstemmingService dokDistAvstemmingService, JiraService jiraService) {
		this.dokDistAvstemmingService = dokDistAvstemmingService;
		this.jiraService = jiraService;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

		scheduledTaskRegistrar.addCronTask(() ->

				{
					try {
						jiraService.createJiraSak();
					} catch (Exception e) {
						log.error(String.format("createJiraSak feilet til å opprette jira sak med feilmelding=%s", e.getMessage()));

					}
				},
				"30 05 15 * * MON-FRI");
	}


}
