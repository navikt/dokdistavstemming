package no.nav.dokdistavstemming;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.metrics.DokMonitoringAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CoreConfig {

	private static final int CORE_POOL_SIZE = 5;
	private static final int MAX_POOL_SIZE = 10;


	@Bean
	public DokMonitoringAspect dokMonitoringAspect(MeterRegistry meterRegistry) {
		return new DokMonitoringAspect(meterRegistry);
	}

	@Bean
	public ThreadPoolTaskExecutor poolTaskExecutor() {
		ThreadPoolTaskExecutor poolTaskExecutor = new ThreadPoolTaskExecutor();
		poolTaskExecutor.setCorePoolSize(CORE_POOL_SIZE);
		poolTaskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
		poolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		poolTaskExecutor.initialize();
		return poolTaskExecutor;
	}
}
