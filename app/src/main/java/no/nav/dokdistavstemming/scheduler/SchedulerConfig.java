package no.nav.dokdistavstemming.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class SchedulerConfig {
	@Bean
	public long initialDelay() {

		return TimeUnit.MINUTES.toMillis(10);
	}

}
