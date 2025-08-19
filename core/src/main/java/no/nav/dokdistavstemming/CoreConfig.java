package no.nav.dokdistavstemming;

import no.nav.dok.jiraapi.JiraProperties;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dok.jiraapi.client.JiraClient;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@Configuration
public class CoreConfig {

	@Bean
	public JiraClient jiraClient(DokdistavstemmingProperties dokdistavstemmingProperties) {
		DokdistavstemmingProperties.JiraUser jiraUser = dokdistavstemmingProperties.getJira();
		return new JiraClient(JiraProperties.builder()
				.jiraServiceUser(new JiraProperties.JiraServiceUser(jiraUser.getUsername(), jiraUser.getPassword()))
				.url(jiraUser.getUrl())
				.build());
	}

	@Bean
	public JiraService jiraService(JiraClient jiraClient) {
		return new JiraService(jiraClient);
	}
}
