package no.nav.dokdistavstemming;

import no.nav.dok.jiraapi.JiraProperties;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dok.jiraapi.client.JiraClient;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.JiraAuthProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.web.client.RestClient;

import java.net.ProxySelector;

import static java.time.Duration.ofSeconds;

@EnableResilientMethods
@Configuration
public class CoreConfig {

	@Bean
	public JiraClient jiraClient(DokdistavstemmingProperties dokdistavstemmingProperties, JiraAuthProperties jiraAuthProperties) {
		DokdistavstemmingProperties.JiraUser jiraUser = dokdistavstemmingProperties.getJira();
		return new JiraClient(JiraProperties.builder()
				.jiraServiceUser(new JiraProperties.JiraServiceUser(jiraAuthProperties.username(), jiraAuthProperties.accessToken()))
				.url(jiraUser.getUrl())
				.build());
	}

	@Bean
	public RestClient restClient() {
		return RestClient.builder()
				.requestFactory(jdkClientHttpRequestFactory())
				.build();
	}

	private JdkClientHttpRequestFactory jdkClientHttpRequestFactory() {
		return ClientHttpRequestFactoryBuilder.jdk()
				.withProxySelector(ProxySelector.getDefault())
				.withCustomizer(jdkClientHttpRequestFactory ->
						jdkClientHttpRequestFactory.setReadTimeout(ofSeconds(20))
				)
				.build();
	}

	@Bean
	public JiraService jiraService(JiraClient jiraClient) {
		return new JiraService(jiraClient);
	}
}
