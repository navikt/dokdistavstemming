package no.nav.dokdistavstemming.config;

import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_CALL_ID;

public class WebClientBasicAuthentication implements ExchangeFilterFunction {

	private final DokdistavstemmingProperties dokdistavstemmingProperties;

	public WebClientBasicAuthentication(DokdistavstemmingProperties dokdistavstemmingProperties) {
		this.dokdistavstemmingProperties = dokdistavstemmingProperties;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(ClientRequest.from(request).headers((headers) -> {
			headers.setBasicAuth(dokdistavstemmingProperties.getServiceuser().getUsername(), dokdistavstemmingProperties.getServiceuser().getPassword());
			headers.set(MDC_CALL_ID, MDC.get(MDC_CALL_ID));
		}).build());
	}
}
