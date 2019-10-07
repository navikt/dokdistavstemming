package no.nav.dokdistavstemming.config;

import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.utils.CallIdInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

	public static final Duration DURATION_READTIMEOUT = Duration.ofMillis(120000L);
	public static final Duration DURATION = Duration.ofMillis(30000l);


	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, final ServiceuserAlias serviceuserAlias) {
		RestTemplate restTemplate = restTemplateBuilder
				.interceptors(new CallIdInterceptor())
				.setReadTimeout(DURATION_READTIMEOUT)
				.setConnectTimeout(DURATION)
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();
		return restTemplate;
	}


}
