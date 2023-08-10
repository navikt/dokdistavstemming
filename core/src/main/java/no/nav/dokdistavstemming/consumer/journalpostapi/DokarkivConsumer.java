package no.nav.dokdistavstemming.consumer.journalpostapi;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.azure.AzureToken;
import no.nav.dokdistavstemming.azure.WebClientAzureAuthentication;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingTechnicalException;
import no.nav.dokdistavstemming.exceptions.JournalpostApiFunctionalException;
import no.nav.dokdistavstemming.exceptions.JournalpostApiTechnicalException;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static no.nav.dokdistavstemming.constants.MDCConstants.DOK_REQUEST;
import static no.nav.dokdistavstemming.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdistavstemming.constants.RetryConstants.MULTIPLIER_SHORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class DokarkivConsumer {

	private final WebClient webClient;

	public DokarkivConsumer(WebClient webClient,
							DokdistavstemmingProperties dokdistavstemmingProp,
							AzureToken azureToken) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdistavstemmingProp.getEndpoints().getDokarkiv().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new WebClientAzureAuthentication(azureToken, dokdistavstemmingProp.getEndpoints().getDokarkiv()))
				.build();
	}

	@Retryable(include = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"process_code", "bulkOppdaterJournalpostDistribusjonsInfo"})
	public String[] finnUlesteJournalposter(DistribusjonKanalCode kanalCode, LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		log.info(String.format("finnUlesteJournalposter har mottatt kall for å finne journalposter fra kanal=%s med ekspedertFra=%s og ekspedertTil=%s.",
				kanalCode.name(), ekspedertFra, ekspedertTil));

		return webClient.post()
				.uri(uriBuilder -> uriBuilder
						.path("/finnUlesteJournalposter/{}/{}/{}")
						.build(kanalCode, ekspedertFra, ekspedertTil)
				)
				.retrieve()
				.bodyToMono(String[].class)
				.doOnError(error -> handleError(error, "finnUlesteJournalposter"))
				.block();
	}

	@Retryable(include = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"process_code", "bulkOppdaterJournalpostDistribusjonsInfo"})
	public BulkOppdaterDistribusjonsinfoResponse bulkOppdaterJournalpostDistribusjonsInfo(BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest) {
		log.info("bulkOppdaterJournalpostDistribusjonsInfo har mottatt kall om å oppdatere distribusjonsinfo på journalposter.");

		return webClient.post()
				.uri("/bulkOppdaterDistribusjonsinfo")
				.body(Mono.just(bulkOppdaterDistribusjonsinfoRequest), BulkOppdaterDistribusjonsinfoRequest.class)
				.retrieve()
				.bodyToMono(BulkOppdaterDistribusjonsinfoResponse.class)
				.doOnError(error -> handleError(error, "bulkOppdaterDistribusjonsinfo"))
				.block();
	}

	@Retryable(include = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"process_code", "oppdaterDistribusjonsinfo"})
	public void oppdaterDistribusjonsinfo(OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest, String journalpostId) {
		log.info(String.format("oppdaterDistribusjonsinfo har mottatt kall om å oppdatere distribusjonsinfo på journalpost={}.", journalpostId));

		webClient.post()
				.uri(uriBuilder -> uriBuilder
						.path("{}/oppdaterDistribusjonsinfo")
						.build(journalpostId))
				.body(Mono.just(oppdaterDistribusjonsinfoRequest), OppdaterDistribusjonsinfoRequest.class)
				.retrieve()
				.bodyToMono(ResponseEntity.class)
				.doOnError(error -> handleError(error, "oppdaterDistribusjonsinfo"))
				.block();
	}

	private void handleError(Throwable error, String endepunkt) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new JournalpostApiFunctionalException(
					format("Kall mot Journalpost-API endepunktet %s feilet med status=%s, feilmelding=%s", endepunkt, response.getRawStatusCode(), response.getMessage()),
					error);
		} else {
			throw new JournalpostApiTechnicalException(
					format("Kall mot Journalpost-API endepunktet %s feilet med feilmelding=%s", endepunkt, error.getMessage()),
					error);
		}
	}
}
