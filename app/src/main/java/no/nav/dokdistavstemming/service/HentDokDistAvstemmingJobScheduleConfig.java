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

	public HentDokDistAvstemmingJobScheduleConfig(DokDistAvstemmingService dokDistAvstemmingService) {
		this.dokDistAvstemmingService = dokDistAvstemmingService;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

		scheduledTaskRegistrar.addCronTask(() ->

				{
					try {
						dokDistAvstemmingService.henteDokDistFil();
					} catch (Exception e) {
						log.error(String.format("createJiraSak feilet til å opprette jira sak med feilmelding=%s", e.getMessage()));

					}
				},
				"30 00 09 * * MON-FRI");
	}


}
