package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.WebClientBasicAuthentication;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.domain.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseTechnicalException;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

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

	private final WebClient webClient;

	@Autowired
	public Rdist001administrerforsendelseConsumer(@Value("${administrerforsendelse.v1.url}") String baseUrl,
												 DokdistavstemmingProperties dokdistavstemmingProperties,
												  WebClient webClient) {
		this.webClient = webClient.mutate()
				.baseUrl(baseUrl)
				.filter(new WebClientBasicAuthentication(dokdistavstemmingProperties))
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Override
	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "hentForsendelserKvitteringIkkeMottatt"})
	public List<AvstemForsendelseRequestTo> hentForsendelserKvitteringIkkeMottatt(String distribusjonKanal, int antallTimer) {
		MDC.put(MDC_CONSUMER_ID, "hentForsendelserKvitteringIkkeMottatt");

		log.info("{} har mottatt kall om å hente forsendelser fra rdist001(dokdist) med distribusjonKanal={}, antallTimer={}",
				MDC.get(MDC_CONSUMER_ID), distribusjonKanal, antallTimer);
		List<AvstemForsendelseRequestTo> avstemForsendelseRequestTos = webClient.get()
				.uri("/henteuekspederforsendelse/{distribusjonKanal}/{antallTimer}", distribusjonKanal, antallTimer)
				.header(MDC_CALL_ID, MDC.get(MDC_CALL_ID))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<AvstemForsendelseRequestTo>>() {
				})
				.doOnError(this::handleError).block();

		return avstemForsendelseRequestTos == null ? Collections.emptyList() : avstemForsendelseRequestTos;
	}

	@Override
	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "oppdaterForsendelserAvstemDatoOgReferanse"})
	public void oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {
		log.info("{} har mottatt kall om å oppdatere forsendelser fra rdist001 med avstemtReferanse={}",
				MDC.get(MDC_CONSUMER_ID), oppdaterForsendelserAvstemtInfo.getAvstemtReferanse());
		webClient.put()
				.uri("/avstemforsendelser")
				.header(MDC_CALL_ID, MDC.get(MDC_CALL_ID))
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
		log.info("{} har mottatt kall om å oppdatere i total {} avstemArkivDato i dokdist database",
				MDC.get(MDC_CONSUMER_ID), avstemEkspederteForsendelserRequest.getForsendelser().size());
		webClient.put()
				.uri("/avstemekspederteforsendelser")
				.header(MDC_CALL_ID, MDC.get(MDC_CALL_ID))
				.body(Mono.just(avstemEkspederteForsendelserRequest), AvstemEkspederteForsendelserRequest.class)
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError).block();
		log.info("Forsendelser med forsendelseIder={} oppdatert", avstemEkspederteForsendelserRequest.getForsendelser().size());
	}

	@Override
	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "DOKDIST", "process_code", "hentEkspederteforsendelser"})
	public HentEkspederteForsendelserResponse hentEkspederteforsendelser() {
		HentEkspederteForsendelserRequest hentEkspederteForsendelserRequest = HentEkspederteForsendelserRequest.builder()
				.maksForsendelser(0)
				.build();
		return webClient.method(GET)
				.uri("/hentekspederteforsendelser")
				.header(MDC_CALL_ID, MDC.get(MDC_CALL_ID))
				.body(Mono.justOrEmpty(hentEkspederteForsendelserRequest), HentEkspederteForsendelserRequest.class)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<HentEkspederteForsendelserResponse>() {
				})
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
