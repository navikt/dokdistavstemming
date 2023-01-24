package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.azure.AzureToken;
import no.nav.dokdistavstemming.azure.WebClientAzureAuthentication;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.WebClientBasicAuthentication;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.domain.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.domain.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseTechnicalException;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

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

	private final WebClient webClientDokumentdistribusjon;
	private final WebClient webClientDokdistadmin;
	private final DokdistavstemmingProperties dokdistavstemmingProperties;

	public Rdist001administrerforsendelseConsumer(@Value("${administrerforsendelse.v1.url}") String baseUrlDokumentdistribusjon,
												  DokdistavstemmingProperties dokdistavstemmingProperties,
												  WebClient webClientDokumentdistribusjon,
												  WebClient webClientDokdistadmin,
												  AzureToken azureToken) {
		this.dokdistavstemmingProperties = dokdistavstemmingProperties;
		this.webClientDokdistadmin = webClientDokdistadmin.mutate()
				.baseUrl(dokdistavstemmingProperties.getEndpoints().getDokdistadmin().getUrl())
				.filter(new WebClientAzureAuthentication(azureToken, dokdistavstemmingProperties.getEndpoints().getDokdistadmin()))
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
		this.webClientDokumentdistribusjon = webClientDokumentdistribusjon.mutate()
				.baseUrl(baseUrlDokumentdistribusjon)
				.filter(new WebClientBasicAuthentication(dokdistavstemmingProperties))
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Override
	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "hentForsendelserKvitteringIkkeMottatt"})
	public List<AvstemForsendelseRequestTo> hentForsendelserKvitteringIkkeMottatt(String distribusjonKanal, int antallTimer) {
		MDC.put(MDC_CONSUMER_ID, "hentForsendelserKvitteringIkkeMottatt");

		// TODO: Refaktorer AvstemForsendelseRequestTo. Litt forvirrende navn

		log.info("hentForsendelserKvitteringIkkeMottatt har mottatt kall om å hente forsendelser fra rdist001(dokdistadmin) med distribusjonKanal={}, antallTimer={}",
				distribusjonKanal, antallTimer);

		HentUekspederteForsendelserResponse response = webClientDokdistadmin.get()
				.uri("/hentuekspederteforsendelser/{distribusjonKanal}/{antallTimer}", distribusjonKanal, antallTimer)
				.retrieve()
				.bodyToMono(HentUekspederteForsendelserResponse.class)
				.doOnError(this::handleError)
				.block();

		// TODO: Endre bruk av HentUekspederteForsendelserResponse og AvstemForsendelseRequestTo videre her
		return response.getUekspedertForsendelseList().stream()
				.map(AvstemForsendelseMapper::fromHentUekspederteForsendelserResponse)
				.toList();
	}

	@Override
	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "oppdaterForsendelserAvstemDatoOgReferanse"})
	public void oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {
		log.info("oppdaterForsendelserAvstemDatoOgReferanse har mottatt kall om å oppdatere forsendelser fra rdist001 med avstemtReferanse={}", oppdaterForsendelserAvstemtInfo.getAvstemtReferanse());
		webClientDokumentdistribusjon.put()
				.uri("/avstemforsendelser")
				.body(Mono.just(oppdaterForsendelserAvstemtInfo), OppdaterForsendelserAvstemtInfo.class)
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError).block();
		log.info("Forsendelser med forsendelseIder={} oppdatert", oppdaterForsendelserAvstemtInfo.getForsendelser().size());
	}

	@Override
	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"process_code", "oppdaterAvstemEkspderteForsendelser"})
	public void oppdaterAvstemEkspederteForsendelser(AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest) {
		log.info("oppdaterAvstemEkspederteForsendelser har mottatt kall om å oppdatere {} forsendelser med avstemArkivDato i dokdist-databasen", avstemEkspederteForsendelserRequest.getForsendelser().size());

		webClientDokdistadmin.put()
				.uri("/avstemekspederteforsendelser")
				.body(Mono.just(avstemEkspederteForsendelserRequest), AvstemEkspederteForsendelserRequest.class)
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.block();

		log.info("avstemekspederteforsendelser har oppdatert {} forsendelser med avstemArkivDato i dokdist-databasen", avstemEkspederteForsendelserRequest.getForsendelser().size());
	}

	@Override
	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "hentEkspederteforsendelser"})
	public HentEkspederteForsendelserResponse hentEkspederteforsendelser() {
		MDC.put(MDC_CALL_ID, UUID.randomUUID().toString());

		// maxForsendelser lik 0 betyr at max forsendelser blir requestet (konfigurert i dokdistadmin)
		HentEkspederteForsendelserRequest hentEkspederteForsendelserRequest = HentEkspederteForsendelserRequest.builder()
				.maksForsendelser(dokdistavstemmingProperties.getSdist004().getMaxForsendelserRequest())
				.build();

		return webClientDokdistadmin.method(GET)
				.uri("/hentekspederteforsendelser")
				.body(Mono.justOrEmpty(hentEkspederteForsendelserRequest), HentEkspederteForsendelserRequest.class)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<HentEkspederteForsendelserResponse>() {})
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new AvstemForsendelseFunctionalException(
					String.format("Kall mot rdist001 feilet med status=%s, feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new AvstemForsendelseTechnicalException(
					String.format("Kall mot rdist001 feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
