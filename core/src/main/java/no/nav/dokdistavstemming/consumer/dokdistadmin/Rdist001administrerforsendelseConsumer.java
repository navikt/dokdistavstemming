package no.nav.dokdistavstemming.consumer.dokdistadmin;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.azure.AzureToken;
import no.nav.dokdistavstemming.azure.WebClientAzureAuthentication;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.FeilregistrerForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTo;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTos;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.Forsendelse;
import no.nav.dokdistavstemming.exceptions.DokdistadminFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokdistadminTechnicalException;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static no.nav.dokdistavstemming.constants.MDCConstants.DOK_REQUEST;
import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_CALL_ID;
import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokdistavstemming.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdistavstemming.constants.RetryConstants.MULTIPLIER_SHORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class Rdist001administrerforsendelseConsumer implements Rdist001administrerforsendelse {

	private final HentUekspederteForsendelserResponse EMPTY_UEKSPEDERTEFORSENDELSER = HentUekspederteForsendelserResponse.builder()
			.uekspederteForsendelser(emptyList())
			.build();

	private final HentEkspederteForsendelserResponse EMPTY_EKSPEDERTEFORSENDELSER = HentEkspederteForsendelserResponse.builder()
			.forsendelser(emptyList())
			.build();

	private final WebClient webClient;
	private final DokdistavstemmingProperties dokdistavstemmingProperties;

	public Rdist001administrerforsendelseConsumer(DokdistavstemmingProperties dokdistavstemmingProperties,
												  WebClient webClient,
												  AzureToken azureToken) {
		this.dokdistavstemmingProperties = dokdistavstemmingProperties;
		this.webClient = webClient.mutate()
				.baseUrl(dokdistavstemmingProperties.getEndpoints().getDokdistadmin().getUrl())
				.filter(new WebClientAzureAuthentication(azureToken, dokdistavstemmingProperties.getEndpoints().getDokdistadmin()))
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Override
	@Retryable(include = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "hentForsendelserKvitteringIkkeMottatt"})
	public HentUekspederteForsendelserResponse hentForsendelserKvitteringIkkeMottatt(String distribusjonskanal, int antallTimer) {
		MDC.put(MDC_CONSUMER_ID, "hentForsendelserKvitteringIkkeMottatt");

		log.info("hentForsendelserKvitteringIkkeMottatt henter forsendelser fra rdist001 (dokdistadmin) med distribusjonskanal={}, antallTimer={}",
				distribusjonskanal, antallTimer);

		return webClient.get()
				.uri("/hentuekspederteforsendelser/{distribusjonkanal}/{antallTimer}", distribusjonskanal, antallTimer)
				.retrieve()
				.bodyToMono(HentUekspederteForsendelserResponse.class)
				.defaultIfEmpty(EMPTY_UEKSPEDERTEFORSENDELSER) // Håndtering av HttpStatus NO_CONTENT (204)
				.doOnError(this::handleError)
				.block();
	}

	@Override
	@Retryable(include = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "oppdaterForsendelserAvstemDatoOgReferanse"})
	public void oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {
		log.info("oppdaterForsendelserAvstemDatoOgReferanse har mottatt kall om å oppdatere {} forsendelser fra rdist001 med avstemtReferanse={}",
				oppdaterForsendelserAvstemtInfo.getForsendelser().size(), oppdaterForsendelserAvstemtInfo.getAvstemtReferanse());

		webClient.put()
				.uri("/avstemforsendelser")
				.body(Mono.just(oppdaterForsendelserAvstemtInfo), OppdaterForsendelserAvstemtInfo.class)
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.block();

		log.info("avstemforsendelser har oppdatert {} forsendelser med avstemtReferanse og avstemtDato", oppdaterForsendelserAvstemtInfo.getForsendelser().size());
	}

	@Override
	@Retryable(include = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"process_code", "oppdaterAvstemEkspderteForsendelser"})
	public void oppdaterAvstemEkspederteForsendelser(AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest) {
		log.info("oppdaterAvstemEkspederteForsendelser har mottatt kall om å oppdatere {} forsendelser med avstemArkivDato i dokdist-databasen", avstemEkspederteForsendelserRequest.getForsendelser().size());

		webClient.put()
				.uri("/avstemekspederteforsendelser")
				.body(Mono.just(avstemEkspederteForsendelserRequest), AvstemEkspederteForsendelserRequest.class)
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.block();

		log.info("avstemekspederteforsendelser har oppdatert {} forsendelser med avstemArkivDato i dokdist-databasen", avstemEkspederteForsendelserRequest.getForsendelser().size());
	}

	@Override
	@Retryable(include = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "hentEkspederteforsendelser"})
	public HentEkspederteForsendelserResponse hentEkspederteforsendelser() {
		MDC.put(MDC_CALL_ID, UUID.randomUUID().toString());

		// maxForsendelser lik 0 betyr at max forsendelser blir requestet (konfigurert i dokdistadmin)
		HentEkspederteForsendelserRequest hentEkspederteForsendelserRequest = HentEkspederteForsendelserRequest.builder()
				.maksForsendelser(dokdistavstemmingProperties.getSdist004().getMaxForsendelserRequest())
				.build();

		return webClient.method(GET)
				.uri("/hentekspederteforsendelser")
				.body(Mono.justOrEmpty(hentEkspederteForsendelserRequest), HentEkspederteForsendelserRequest.class)
				.retrieve()
				.bodyToMono(HentEkspederteForsendelserResponse.class)
				.defaultIfEmpty(EMPTY_EKSPEDERTEFORSENDELSER) // Håndtering av HttpStatus NO_CONTENT (204)
				.doOnError(this::handleError)
				.block();
	}

	@Override
	@Retryable(include = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "hentForsendelser"})
	public Optional<ForsendelseTos> hentForsendelser(HentForsendelseRequest hentForsendelseRequest) {
		log.info(String.format("hentForsendelser henter forsendelser for journalpostIder={}", String.join(",", hentForsendelseRequest.getJournalpostliste())));

		return Optional.ofNullable(webClient.method(GET)
				.body(Mono.justOrEmpty(hentForsendelseRequest), HentForsendelseRequest.class)
				.retrieve()
				.bodyToMono(ForsendelseTos.class)
				.doOnError(this::handleError)
				.block());
	}

	@Override
	@Retryable(include = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "opprettForsendelse"})
	public Forsendelse opprettForsendelse(ForsendelseTo forsendelseTo) {
		log.info(String.format("opprettForsendelse oppretter forsendelse for bestillingsId={}", forsendelseTo.getBestillingsId()));

		return webClient.put()
				.uri("/")
				.body(Mono.justOrEmpty(forsendelseTo), Forsendelse.class)
				.retrieve()
				.bodyToMono(Forsendelse.class)
				.doOnError(this::handleError)
				.block();
	}

	@Override
	@Retryable(include = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "feilregistrerForsendelse"})
	public void feilregistrerForsendelse(FeilregistrerForsendelseRequest feilregistrerForsendelseRequest) {
		log.info(String.format("FeilregistrerForsendelse feilregistrerer forsendelsesId={}", feilregistrerForsendelseRequest.getForsendelseId()));

		webClient.put()
				.uri("/feilregistrerforsendelse")
				.body(Mono.justOrEmpty(feilregistrerForsendelseRequest), FeilregistrerForsendelseRequest.class)
				.retrieve()
				//response fra dokdistadmin er bare en tom responseEntity med 200 OK
				.bodyToMono(ResponseEntity.class)
				.doOnError(this::handleError)
				.block();
	}

	@Override
	@Retryable(include = DokdistadminTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "oppdaterForsendelse"})
	public void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelseRequest) {
		log.info(String.format("oppdaterForsendelse opptarerer forsendelse med forsendelsesId={}", oppdaterForsendelseRequest.getForsendelseId()));
		webClient.put()
				.uri("/oppdaterForsendelse")
				.body(Mono.justOrEmpty(oppdaterForsendelseRequest), OppdaterForsendelseRequest.class)
				.retrieve()
				//response fra dokdistadmin er bare en tom responseEntity med 200 OK
				.bodyToMono(ResponseEntity.class)
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new DokdistadminFunctionalException(
					String.format("Kall mot rdist001 feilet med status=%s, feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new DokdistadminTechnicalException(
					String.format("Kall mot rdist001 feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
