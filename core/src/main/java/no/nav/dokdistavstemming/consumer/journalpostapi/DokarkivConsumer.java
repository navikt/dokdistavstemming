package no.nav.dokdistavstemming.consumer.journalpostapi;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.domain.enums.UtsendingsKanalCode;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.time.Duration.ofSeconds;
import static no.nav.dokdistavstemming.config.OAuth2WebClientConfig.CLIENT_REGISTRATION_DOKARKIV;
import static no.nav.dokdistavstemming.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdistavstemming.constants.RetryConstants.MULTIPLIER_SHORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class DokarkivConsumer {

	private final WebClient webClient;
	private final String JOURNALPOST_API_URL = "/journalpostapi/v1";
	private final String JOURNALPOST_API_JOURNALPOST_URL = JOURNALPOST_API_URL + "/journalpost";
	private final String SIKKERHETSNIVAA_API_URL = "/internal/sikkerhetsnivaa";

	public DokarkivConsumer(WebClient webClient,
							DokdistavstemmingProperties dokdistavstemmingProp) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdistavstemmingProp.getEndpoints().getDokarkiv().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	public List<String> finnUlesteJournalposter(UtsendingsKanalCode kanalCode, LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		log.info("finnUlesteJournalposter ser etter journalposter med kanal={}, ekspedertFra={}, ekspedertTil={}",
				kanalCode.name(), ekspedertFra, ekspedertTil);

		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(SIKKERHETSNIVAA_API_URL + "/finnUlesteJournalposter/{kanalCode}/{ekspedertFra}/{ekspedertTil}")
						.build(kanalCode, ekspedertFra, ekspedertTil)
				)
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(ofSeconds(360));
				})
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<String>>() {
				})
				.onErrorMap(this::mapErrors)
				.defaultIfEmpty(List.of())
				.block();
	}

	@Retryable(includes = DokdistavstemmingTechnicalException.class, delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT)
	public BulkOppdaterDistribusjonsinfoResponse bulkOppdaterJournalpostDistribusjonsInfo(BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest) {
		log.info("bulkOppdaterJournalpostDistribusjonsInfo har mottatt kall om å oppdatere distribusjonsinfo på journalposter.");

		return webClient.post()
				.uri(JOURNALPOST_API_URL + "/bulkOppdaterDistribusjonsinfo")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.body(Mono.just(bulkOppdaterDistribusjonsinfoRequest), BulkOppdaterDistribusjonsinfoRequest.class)
				.retrieve()
				.bodyToMono(BulkOppdaterDistribusjonsinfoResponse.class)
				.onErrorMap(this::mapErrors)
				.block();
	}

	@Retryable(includes = DokdistavstemmingTechnicalException.class, delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT)
	public void oppdaterDistribusjonsinfo(OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest, String journalpostId) {
		log.info("oppdaterDistribusjonsinfo oppdaterer distribusjonsinfo for journalpost={}.", journalpostId);

		webClient.patch()
				.uri(uriBuilder -> uriBuilder
						.path(JOURNALPOST_API_JOURNALPOST_URL + "/{journalpostId}/oppdaterDistribusjonsinfo")
						.build(journalpostId))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.body(Mono.just(oppdaterDistribusjonsinfoRequest), OppdaterDistribusjonsinfoRequest.class)
				.retrieve()
				.bodyToMono(Void.class)
				.onErrorMap(this::mapErrors)
				.block();
	}

	private Throwable mapErrors(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new DokdistavstemmingFunctionalException(
					format("Kall mot Journalpost-API feilet med status=%s, feilmelding=%s", response.getStatusCode(), response.getMessage()),
					error);
		} else {
			return new DokdistavstemmingTechnicalException(
					format("Kall mot Journalpost-API feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
