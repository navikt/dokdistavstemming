package no.nav.dokdistavstemming.consumer.dokdistadmin;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.FeilregistrerForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTo;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTos;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.Forsendelse;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingTechnicalException;
import org.slf4j.MDC;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.time.Duration.ofSeconds;
import static no.nav.dokdistavstemming.config.OAuth2WebClientConfig.CLIENT_REGISTRATION_DOKDISTADMIN;
import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_CALL_ID;
import static no.nav.dokdistavstemming.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdistavstemming.constants.RetryConstants.MULTIPLIER_SHORT;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.DITTNAV;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonsTypeKode.VEDTAK;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonsTypeKode.VIKTIG;
import static no.nav.dokdistavstemming.domain.enums.DokumentStatusCode.EKSPEDERT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class DokdistadminConsumer implements DokdistadminRdist001Api {

	//max url-lengde i nginx er 8k tegn, apache håndterer ca 6k (+- 6.15kb)
	//200 journalposter gir en url på ca 5.7k tegn og 5.5kb i size
	public static int HENTFORSENDELSER_MAX_JOURNALPOSTS = 200;

	private final WebClient webClient;
	private final DokdistavstemmingProperties dokdistavstemmingProperties;

	public DokdistadminConsumer(DokdistavstemmingProperties dokdistavstemmingProperties,
								WebClient webClient) {
		this.dokdistavstemmingProperties = dokdistavstemmingProperties;
		this.webClient = webClient.mutate()
				.baseUrl(dokdistavstemmingProperties.getEndpoints().getDokdistadmin().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Override
	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public HentUekspederteForsendelserResponse hentForsendelserKvitteringIkkeMottatt(String distribusjonskanal, int antallTimer) {
		log.info("hentForsendelserKvitteringIkkeMottatt henter forsendelser fra rdist001 (dokdistadmin) med distribusjonskanal={}, antallTimer={}",
				distribusjonskanal, antallTimer);

		return webClient.get()
				.uri("/hentuekspederteforsendelser/{distribusjonkanal}/{antallTimer}", distribusjonskanal, antallTimer)
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(ofSeconds(240));
				})
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.retrieve()
				.bodyToMono(HentUekspederteForsendelserResponse.class)
				.defaultIfEmpty(HentUekspederteForsendelserResponse.empty()) // Håndtering av HttpStatus NO_CONTENT (204)
				.onErrorMap(this::mapError)
				.block();
	}

	@Override
	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public void oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {
		log.info("oppdaterForsendelserAvstemDatoOgReferanse har mottatt kall om å oppdatere {} forsendelser fra rdist001 med avstemtReferanse={}",
				oppdaterForsendelserAvstemtInfo.getForsendelser().size(), oppdaterForsendelserAvstemtInfo.getAvstemtReferanse());

		webClient.put()
				.uri("/avstemforsendelser")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.body(Mono.just(oppdaterForsendelserAvstemtInfo), OppdaterForsendelserAvstemtInfo.class)
				.retrieve()
				.toBodilessEntity()
				.onErrorMap(this::mapError)
				.block();

		log.info("avstemforsendelser har oppdatert {} forsendelser med avstemtReferanse og avstemtDato", oppdaterForsendelserAvstemtInfo.getForsendelser().size());
	}

	@Override
	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public void oppdaterAvstemEkspederteForsendelser(AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest) {
		log.info("oppdaterAvstemEkspederteForsendelser har mottatt kall om å oppdatere {} forsendelser med avstemArkivDato i dokdist-databasen", avstemEkspederteForsendelserRequest.getForsendelser().size());

		webClient.put()
				.uri("/avstemekspederteforsendelser")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.body(Mono.just(avstemEkspederteForsendelserRequest), AvstemEkspederteForsendelserRequest.class)
				.retrieve()
				.toBodilessEntity()
				.onErrorMap(this::mapError)
				.block();

		log.info("avstemekspederteforsendelser har oppdatert {} forsendelser med avstemArkivDato i dokdist-databasen", avstemEkspederteForsendelserRequest.getForsendelser().size());
	}

	@Override
	public HentEkspederteForsendelserResponse hentEkspederteforsendelser() {
		MDC.put(MDC_CALL_ID, UUID.randomUUID().toString());

		// maxForsendelser lik 0 betyr at max forsendelser blir requestet (konfigurert i dokdistadmin)
		HentEkspederteForsendelserRequest hentEkspederteForsendelserRequest = HentEkspederteForsendelserRequest.builder()
				.maksForsendelser(dokdistavstemmingProperties.getSdist004().getMaxForsendelserRequest())
				.build();

		return webClient.method(GET)
				.uri("/hentekspederteforsendelser")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.body(Mono.justOrEmpty(hentEkspederteForsendelserRequest), HentEkspederteForsendelserRequest.class)
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(ofSeconds(180));
				})
				.retrieve()
				.bodyToMono(HentEkspederteForsendelserResponse.class)
				.defaultIfEmpty(HentEkspederteForsendelserResponse.empty()) // Håndtering av HttpStatus NO_CONTENT (204)
				.onErrorMap(this::mapError)
				.block();
	}

	@Override
	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public Optional<ForsendelseTos> hentForsendelser(List<String> journalpostListe) {
		return Optional.ofNullable(
				webClient.method(GET)
						.uri(uriBuilder -> uriBuilder
								.path("/hentForsendelser")
								.queryParam("distribusjonstyper", List.of(VIKTIG, VEDTAK))
								.queryParam("dokumentstatus", EKSPEDERT)
								.queryParam("distribusjonkanal", DITTNAV)
								.queryParam("journalpostliste", journalpostListe)
								.build()
						)
						.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
						.retrieve()
						.bodyToMono(ForsendelseTos.class)
						.onErrorMap(this::mapError)
						.block());
	}

	@Override
	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public Forsendelse opprettForsendelse(ForsendelseTo forsendelseTo) {
		log.info("opprettForsendelse oppretter forsendelse for bestillingsId={}", forsendelseTo.getBestillingsId());

		return webClient.post()
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.body(Mono.justOrEmpty(forsendelseTo), ForsendelseTo.class)
				.retrieve()
				.bodyToMono(Forsendelse.class)
				.onErrorMap(this::mapError)
				.block();
	}

	@Override
	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public void feilregistrerForsendelse(FeilregistrerForsendelseRequest feilregistrerForsendelseRequest) {
		log.info("feilregistrerForsendelse feilregistrerer forsendelsesId={}", feilregistrerForsendelseRequest.getForsendelseId());

		webClient.put()
				.uri("/feilregistrerforsendelse")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.body(Mono.justOrEmpty(feilregistrerForsendelseRequest), FeilregistrerForsendelseRequest.class)
				.retrieve()
				//response fra dokdistadmin er bare en tom responseEntity med 200 OK
				.bodyToMono(Void.class)
				.onErrorMap(this::mapError)
				.block();
	}

	@Override
	@Retryable(retryFor = DokdistavstemmingTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelseRequest) {
		log.info("oppdaterForsendelse oppdaterer forsendelse med forsendelsesId={}", oppdaterForsendelseRequest.getForsendelseId());

		webClient.put()
				.uri("/oppdaterforsendelse")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKDISTADMIN))
				.body(Mono.justOrEmpty(oppdaterForsendelseRequest), OppdaterForsendelseRequest.class)
				.retrieve()
				//response fra dokdistadmin er bare en tom responseEntity med 200 OK
				.bodyToMono(Void.class)
				.onErrorMap(this::mapError)
				.block();
	}

	private Throwable mapError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new DokdistavstemmingFunctionalException(
					format("Kall mot rdist001 feilet med status=%s, feilmelding=%s", response.getStatusCode(), response.getMessage()),
					error);
		} else {
			return new DokdistavstemmingTechnicalException(
					format("Kall mot rdist001 feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
