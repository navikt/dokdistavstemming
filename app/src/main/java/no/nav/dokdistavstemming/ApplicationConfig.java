package no.nav.dokdistavstemming;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.azure.AzureConfig;
import no.nav.dokdistavstemming.config.DokdistavstemmingProp;
import no.nav.dokdistavstemming.config.WebClientConfig;
import no.nav.dokdistavstemming.config.alias.JiraServiceuserAlias;
import no.nav.dokdistavstemming.config.alias.ServiceuserProperties;
import no.nav.dokdistavstemming.metrics.DokMonitoringAspect;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableConfigurationProperties(value = {
		ServiceuserProperties.class,
		JiraServiceuserAlias.class,
		DokdistavstemmingProp.class,
		WebClientConfig.class,
		AzureConfig.class
})
@EnableAspectJAutoProxy
@EnableRetry
@EnableScheduling
public class ApplicationConfig {
	@Bean
	public DokMonitoringAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokMonitoringAspect(meterRegistry);
	}
}
