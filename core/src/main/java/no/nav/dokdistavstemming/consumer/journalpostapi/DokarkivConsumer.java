package no.nav.dokdistavstemming.consumer.journalpostapi;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.azure.AzureToken;
import no.nav.dokdistavstemming.azure.WebClientAzureAuthentication;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.domain.enums.UtsendingsKanalCode;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.time.Duration.ofSeconds;
import static no.nav.dokdistavstemming.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdistavstemming.constants.RetryConstants.MULTIPLIER_SHORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class DokarkivConsumer {

	private final WebClient webClient;
	private final String JOURNALPOST_API_URL = "/journalpostapi/v1";
	private final String JOURNALPOST_API_JOURNALPOST_URL = JOURNALPOST_API_URL + "/journalpost";
	private final String SIKKERHETSNIVAA_API_URL = "/internal/sikkerhetsnivaa";

	public DokarkivConsumer(WebClient webClient,
							DokdistavstemmingProperties dokdistavstemmingProp,
							AzureToken azureToken) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdistavstemmingProp.getEndpoints().getDokarkiv().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new WebClientAzureAuthentication(azureToken, dokdistavstemmingProp.getEndpoints().getDokarkiv()))
				.build();
	}

	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public List<String> finnUlesteJournalposter(UtsendingsKanalCode kanalCode, LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		log.info(String.format("finnUlesteJournalposter har mottatt kall for å finne journalposter fra kanal=%s med ekspedertFra=%s og ekspedertTil=%s.",
				kanalCode.name(), ekspedertFra, ekspedertTil));

		log.info("Kaller dokarkiv med ekspedertFra={}, ekspedertTil={}", ekspedertFra, ekspedertTil);

		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(SIKKERHETSNIVAA_API_URL + "/finnUlesteJournalposter/{kanalCode}/{ekspedertFra}/{ekspedertTil}")
						.build(kanalCode, ekspedertFra, ekspedertTil)
				)
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(ofSeconds(120));
				})
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<String>>() {
				})
				.doOnError(this::handleError)
				.block();
	}

	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public BulkOppdaterDistribusjonsinfoResponse bulkOppdaterJournalpostDistribusjonsInfo(BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest) {
		log.info("bulkOppdaterJournalpostDistribusjonsInfo har mottatt kall om å oppdatere distribusjonsinfo på journalposter.");

		return webClient.post()
				.uri(JOURNALPOST_API_URL + "/bulkOppdaterDistribusjonsinfo")
				.body(Mono.just(bulkOppdaterDistribusjonsinfoRequest), BulkOppdaterDistribusjonsinfoRequest.class)
				.retrieve()
				.bodyToMono(BulkOppdaterDistribusjonsinfoResponse.class)
				.doOnError(this::handleError)
				.block();
	}

	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public void oppdaterDistribusjonsinfo(OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest, String journalpostId) {
		log.info(String.format("Sdist006 oppdaterer distribusjonsinfo for journalpost=%s.", journalpostId));

		webClient.patch()
				.uri(uriBuilder -> uriBuilder
						.path(JOURNALPOST_API_JOURNALPOST_URL + "/{journalpostId}/oppdaterDistribusjonsinfo")
						.build(journalpostId))
				.body(Mono.just(oppdaterDistribusjonsinfoRequest), OppdaterDistribusjonsinfoRequest.class)
				.retrieve()
				.bodyToMono(String.class)
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new DokdistavstemmingFunctionalException(
					format("Kall mot Journalpost-API feilet med status=%s, feilmelding=%s", response.getStatusCode(), response.getMessage()),
					error);
		} else {
			throw new DokdistavstemmingTechnicalException(
					format("Kall mot Journalpost-API feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
