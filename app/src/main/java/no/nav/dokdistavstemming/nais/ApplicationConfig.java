package no.nav.dokdistavstemming.nais;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.metrics.DokMonitoringAspect;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;


@ComponentScan(basePackages = "no.nav.dokdistavstemming")
@Configuration
@EnableConfigurationProperties(value = {ServiceuserAlias.class})
@EnableAspectJAutoProxy
@EnableRetry
public class ApplicationConfig {

	@Bean
	public DokMonitoringAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokMonitoringAspect(meterRegistry);
	}
}
