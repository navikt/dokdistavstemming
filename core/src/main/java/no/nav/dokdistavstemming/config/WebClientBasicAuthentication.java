package no.nav.dokdistavstemming.config;

import no.nav.dokdistavstemming.config.alias.ServiceuserProperties;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class WebClientBasicAuthentication implements ExchangeFilterFunction {

	private final ServiceuserProperties serviceuserProperties;

	public WebClientBasicAuthentication(ServiceuserProperties serviceuserProperties) {
		this.serviceuserProperties = serviceuserProperties;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(ClientRequest.from(request).headers((headers) -> {
			headers.setBasicAuth(serviceuserProperties.getUsername(), serviceuserProperties.getPassword());
		}).build());
	}
}
