package no.nav.dokdistavstemming.nais;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.DokdistavstemmingProp;
import no.nav.dokdistavstemming.config.alias.JiraServiceuserAlias;
import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.metrics.DokMonitoringAspect;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableConfigurationProperties(value = {ServiceuserAlias.class, JiraServiceuserAlias.class, DokdistavstemmingProp.class})
@EnableAspectJAutoProxy
@EnableRetry
@EnableScheduling
public class ApplicationConfig {

	@Bean
	public DokMonitoringAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokMonitoringAspect(meterRegistry);
	}



}
