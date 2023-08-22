package no.nav.dokdistavstemming.azure;


import no.nav.dokdistavstemming.constants.MDCConstants;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class WebClientAzureAuthentication implements ExchangeFilterFunction {

	private final AzureToken azureToken;
	private final DokdistavstemmingProperties.AzureEndpoint endpoint;

	public WebClientAzureAuthentication(AzureToken azureToken, DokdistavstemmingProperties.AzureEndpoint endpoint) {
		this.azureToken = azureToken;
		this.endpoint = endpoint;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(ClientRequest.from(request)
				.headers(httpHeaders -> {
					httpHeaders.setBearerAuth(azureToken.accessToken(endpoint.getScope()));
					httpHeaders.set(MDCConstants.MDC_CALL_ID, MDC.get(MDCConstants.MDC_CALL_ID));
				})
				.build());
	}
}
