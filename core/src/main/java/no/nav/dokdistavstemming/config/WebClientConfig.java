package no.nav.dokdistavstemming.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.validation.constraints.NotEmpty;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Data
@Configuration
@ConfigurationProperties("administrerforsendelse.v1")
@Validated
public class WebClientConfig {

	@NotEmpty
	private String url;

	@Bean("rdist001Client")
	public WebClient rdist001Client(WebClient.Builder webClientBuilder) {
		return webClientBuilder
				.clone()
				.baseUrl(url)
				.clientConnector(new ReactorClientHttpConnector(httpClient()))
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	private HttpClient httpClient() {
		return HttpClient.create().proxyWithSystemProperties();
	}
}

