package no.nav.dokdistavstemming.azure;


import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_CALL_ID;

public class WebClientAzureAuthentication implements ExchangeFilterFunction {

	private final AzureToken azureToken;
	private final DokdistavstemmingProperties dokdistavstemmingProp;

	public WebClientAzureAuthentication(AzureToken azureToken, DokdistavstemmingProperties dokdistavstemmingProp) {
		this.azureToken = azureToken;
		this.dokdistavstemmingProp = dokdistavstemmingProp;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(ClientRequest.from(request)
				.headers(httpHeaders -> {
					httpHeaders.setBearerAuth(azureToken.accessToken(dokdistavstemmingProp.getEndpoints().getDokarkiv().getScope()));
					httpHeaders.set(MDC_CALL_ID, MDC.get(MDC_CALL_ID));
				})
				.build());
	}
}
