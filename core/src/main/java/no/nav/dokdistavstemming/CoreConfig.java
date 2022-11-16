package no.nav.dokdistavstemming;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.metrics.DokMonitoringAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class CoreConfig {
	@Bean
	public DokMonitoringAspect dokMonitoringAspect(MeterRegistry meterRegistry) {
		return new DokMonitoringAspect(meterRegistry);
	}
}
