package no.nav.dokdistavstemming.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ClientCredentialsReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;

@Configuration
public class OAuth2WebClientConfig {
	public static final String CLIENT_REGISTRATION_DOKDISTADMIN = "azure-dokdistadmin";
	public static final String CLIENT_REGISTRATION_DOKARKIV = "azure-dokarkiv";
	private static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024;

	@Bean
	WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	@Primary
	WebClient webClient(WebClient.Builder webClientBuilder, ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
		var filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
		return webClientBuilder
				.clone()
				.codecs(clientCodec -> clientCodec.defaultCodecs().maxInMemorySize(MAX_BUFFER_SIZE))
				.filter(filter)
				.build();
	}

	@Bean
	ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager(
			ReactiveClientRegistrationRepository clientRegistrationRepository,
			ReactiveOAuth2AuthorizedClientService oAuth2AuthorizedClientService,
			ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsAccessTokenResponseClient
	) {
		var authorizedClientProvider = new ClientCredentialsReactiveOAuth2AuthorizedClientProvider();
		authorizedClientProvider.setAccessTokenResponseClient(clientCredentialsAccessTokenResponseClient);

		var authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
		return authorizedClientManager;
	}

	@Bean
	ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsAccessTokenResponseClient() {
		var accessTokenResponseClient = new WebClientReactiveClientCredentialsTokenResponseClient();
		accessTokenResponseClient.setWebClient(accessTokenWebClient());
		return accessTokenResponseClient;
	}

	@Bean
	ReactiveOAuth2AuthorizedClientService oAuth2AuthorizedClientService(ReactiveClientRegistrationRepository clientRegistrationRepository) {
		return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
	}

	@Bean
	ReactiveClientRegistrationRepository clientRegistrationRepository(List<ClientRegistration> clientRegistration) {
		return new InMemoryReactiveClientRegistrationRepository(clientRegistration);
	}

	@Bean
	List<ClientRegistration> clientRegistration(AzureProperties azureProperties,
												DokdistavstemmingProperties dokdistavstemmingProperties) {
		return List.of(ClientRegistration.withRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN)
						.tokenUri(azureProperties.openidConfigTokenEndpoint())
						.clientId(azureProperties.appClientId())
						.clientSecret(azureProperties.appClientSecret())
						.clientAuthenticationMethod(CLIENT_SECRET_BASIC)
						.authorizationGrantType(CLIENT_CREDENTIALS)
						.scope(dokdistavstemmingProperties.getEndpoints().getDokdistadmin().getScope())
						.build(),
				ClientRegistration.withRegistrationId(CLIENT_REGISTRATION_DOKARKIV)
						.tokenUri(azureProperties.openidConfigTokenEndpoint())
						.clientId(azureProperties.appClientId())
						.clientSecret(azureProperties.appClientSecret())
						.clientAuthenticationMethod(CLIENT_SECRET_BASIC)
						.authorizationGrantType(CLIENT_CREDENTIALS)
						.scope(dokdistavstemmingProperties.getEndpoints().getDokarkiv().getScope())
						.build());
	}

	static WebClient accessTokenWebClient() {
		var clientHttpConnector = new ReactorClientHttpConnector(nettyProxyHttpClient());
		return WebClient.builder()
				.clientConnector(clientHttpConnector)
				.build();
	}

	static HttpClient nettyProxyHttpClient() {
		return HttpClient.create()
				.proxyWithSystemProperties()
				.responseTimeout(Duration.ofSeconds(20));
	}
}

