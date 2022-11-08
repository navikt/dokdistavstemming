package no.nav.dokdistavstemming;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dokdistavstemming.azure.AzureConfig;
import no.nav.dokdistavstemming.config.AvstemForsendelseConfig;
import no.nav.dokdistavstemming.config.DokdistavstemmingProp;
import no.nav.dokdistavstemming.config.WebClientConfig;
import no.nav.dokdistavstemming.config.alias.JiraServiceuserAlias;
import no.nav.dokdistavstemming.config.alias.ServiceuserProperties;
import no.nav.dokdistavstemming.metrics.DokMonitoringAspect;
import no.nav.dokdistavstemming.scheduler.Sdist002ScheduleConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableRetry
@EnableScheduling
@EnableConfigurationProperties(value = {
		ServiceuserProperties.class,
		JiraServiceuserAlias.class,
		DokdistavstemmingProp.class,
		AzureConfig.class
})
@EnableAspectJAutoProxy
@Import(value = {
		WebClientConfig.class,
		Sdist002ScheduleConfig.class,
		AvstemForsendelseConfig.class
})
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
public class CoreConfig {

	@Bean
	public DokMonitoringAspect dokMonitoringAspect(MeterRegistry meterRegistry) {
		return new DokMonitoringAspect(meterRegistry);
	}
}
