package no.nav.dokdistavstemming.azure;


import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

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
				.headers((httpHeaders -> httpHeaders.setBearerAuth(azureToken.accessToken(dokdistavstemmingProp.getEndpoints().getDokarkiv().getScope()))))
				.build());
	}
}
