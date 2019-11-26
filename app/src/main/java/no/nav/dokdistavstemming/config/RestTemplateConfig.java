package no.nav.dokdistavstemming.config;

import no.nav.dokdistavstemming.config.alias.JiraServiceuserAlias;
import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.utils.CallIdInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

	public static final Duration DURATION_READTIMEOUT = Duration.ofMillis(180000L);
	public static final Duration DURATION = Duration.ofMillis(30000l);
	public static final Duration DURATION_READTIMEOUT_JIRA = Duration.ofMillis(15000L);
	public static final Duration DURATION_JIRA = Duration.ofMillis(15000l);


	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, final ServiceuserAlias serviceuserAlias) {
		return restTemplateBuilder
				.interceptors(new CallIdInterceptor())
				.setReadTimeout(DURATION_READTIMEOUT)
				.setConnectTimeout(DURATION)
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();

	}

	@Bean
	public RestTemplate jiraRestTemplate(RestTemplateBuilder restTemplateBuilder, final JiraServiceuserAlias jiraServiceuserAlias) {
		return restTemplateBuilder
				.interceptors(new CallIdInterceptor())
				.setReadTimeout(DURATION_READTIMEOUT_JIRA)
				.setConnectTimeout(DURATION_JIRA)
				.basicAuthentication(jiraServiceuserAlias.getUsername(), jiraServiceuserAlias.getPassword())
				.build();
	}

}
