package no.nav.dokdistavstemming.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

	private static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024;

	@Bean
	@Primary
	public WebClient webClient(WebClient.Builder webClientBuilder) {
		return webClientBuilder
				.clone()
				.clientConnector(new ReactorClientHttpConnector(httpClient()))
				.codecs(clientCodec -> clientCodec.defaultCodecs().maxInMemorySize(MAX_BUFFER_SIZE))
				.build();
	}

	private HttpClient httpClient() {
		return HttpClient.create().proxyWithSystemProperties();
	}
}

