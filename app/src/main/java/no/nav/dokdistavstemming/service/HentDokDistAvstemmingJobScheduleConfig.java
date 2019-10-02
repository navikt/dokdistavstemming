package no.nav.dokdistavstemming.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelseConsumer;
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
	private final HentUekspederForsendelseConsumer hentUekspederForsendelseConsumer;

	public HentDokDistAvstemmingJobScheduleConfig(DokDistAvstemmingService dokDistAvstemmingService, JiraService jiraService,
												  HentUekspederForsendelseConsumer hentUekspederForsendelseConsumer) {
		this.dokDistAvstemmingService = dokDistAvstemmingService;
		this.jiraService = jiraService;
		this.hentUekspederForsendelseConsumer = hentUekspederForsendelseConsumer;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

		scheduledTaskRegistrar.addCronTask(() ->

				{
					try {
						jiraService.createJiraSak();
					} catch (Exception e) {
						e.printStackTrace();
					}
				},
				"30 15 21 * * MON-FRI");
	}


}
