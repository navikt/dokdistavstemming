package no.nav.dokdistavstemming;

import no.nav.dok.jiraapi.JiraProperties;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dok.jiraapi.JiraServiceImp;
import no.nav.dok.jiraapi.client.JiraClient;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableRetry
@Configuration
public class CoreConfig {

	private static final int MAX_POOL_SIZE = 10;

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

	@Bean
	public JiraClient jiraClient(DokdistavstemmingProperties dokdistavstemmingProperties) {
		DokdistavstemmingProperties.JiraUser jiraUser = dokdistavstemmingProperties.getJira();
		return new JiraClient(JiraProperties.builder()
				.jiraServieUser(new JiraProperties.JiraServieUser(jiraUser.getUsername(),jiraUser.getPassword()))
				.url(jiraUser.getUrl())
				.build());
	}

	@Bean
	public JiraService jiraService(JiraClient jiraClient) {
		return new JiraServiceImp(jiraClient);
	}
}
