package no.nav.dokdistavstemming;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("itest")
public class MeterRegisteryConfig {
	@Bean
	public MeterRegistry meterRegistry() {
		return new SimpleMeterRegistry();
	}
}
