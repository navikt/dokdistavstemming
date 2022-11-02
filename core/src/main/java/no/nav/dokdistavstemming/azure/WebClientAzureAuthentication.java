package no.nav.dokdistavstemming.azure;


import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class WebClientAzureAuthentication implements ExchangeFilterFunction {

	private final AzureToken azureToken;

	public WebClientAzureAuthentication(AzureToken azureToken) {
		this.azureToken = azureToken;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(ClientRequest.from(request)
				.headers((httpHeaders -> httpHeaders.setBearerAuth(azureToken.accessToken())))
				.build());
	}
}
