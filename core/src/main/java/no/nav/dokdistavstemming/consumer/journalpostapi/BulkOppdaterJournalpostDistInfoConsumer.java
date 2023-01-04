package no.nav.dokdistavstemming.consumer.journalpostapi;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.azure.AzureToken;
import no.nav.dokdistavstemming.azure.WebClientAzureAuthentication;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseTechnicalException;
import no.nav.dokdistavstemming.exceptions.JournalpostApiFunctionalException;
import no.nav.dokdistavstemming.exceptions.JournalpostApiTechnicalException;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static java.lang.String.format;
import static no.nav.dokdistavstemming.constants.MDCConstants.DOK_REQUEST;
import static no.nav.dokdistavstemming.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdistavstemming.constants.RetryConstants.MULTIPLIER_SHORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class BulkOppdaterJournalpostDistInfoConsumer {

	private final WebClient webClient;

	public BulkOppdaterJournalpostDistInfoConsumer(WebClient webClient,
												   DokdistavstemmingProperties dokdistavstemmingProp,
												   AzureToken azureToken) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdistavstemmingProp.getEndpoints().getDokarkiv().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new WebClientAzureAuthentication(azureToken, dokdistavstemmingProp))
				.build();
	}

	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"process_code", "bulkOppdaterJournalpostDistribusjonsInfo"})
	public BulkOppdaterDistribusjonsinfoResponse bulkOppdaterJournalpostDistribusjonsInfo(BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest) {
		log.info("bulkOppdaterJournalpostDistribusjonsInfo har mottatt kall til å oppdatere journalposter distribusjonsinfo.");
		return webClient.post()
				.uri("/bulkOppdaterDistribusjonsinfo")
				.body(Mono.just(bulkOppdaterDistribusjonsinfoRequest), BulkOppdaterDistribusjonsinfoRequest.class)
				.retrieve()
				.bodyToMono(BulkOppdaterDistribusjonsinfoResponse.class)
				.doOnError(this::handleError).block();
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new JournalpostApiFunctionalException(
					format("Kall mot JournalpostAPI feilet med status=%s, feilmelding=%s", response.getRawStatusCode(), response.getMessage()),
					error);
		} else {
			throw new JournalpostApiTechnicalException(
					format("Kall mot JournalpostAPI feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
