package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.sdist002.CSVProdusere;
import no.nav.dokdistavstemming.sdist002.serviceimp.CSVProdusereImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableRetry
@Configuration
public class CoreConfig {

	private static final int MAX_POOL_SIZE = 10;

	@Bean
	public CSVProdusere csvProdusere() {
		return new CSVProdusereImpl();
	}

	@Bean
	public ThreadPoolTaskExecutor poolTaskExecutor() {
		ThreadPoolTaskExecutor poolTaskExecutor = new ThreadPoolTaskExecutor();
		poolTaskExecutor.setCorePoolSize(MAX_POOL_SIZE);
		poolTaskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
		poolTaskExecutor.setThreadNamePrefix("dokdistavstemming-task-pool-");
		poolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		poolTaskExecutor.initialize();
		return poolTaskExecutor;
	}
}
