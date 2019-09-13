package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingForsendelse;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingTechnicalException;
import no.nav.dokdistavstemming.mdc.MDCConstants;
import no.nav.dokdistavstemming.metrics.Monitor;
import no.nav.dokdistavstemming.utils.CallIdInterceptor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
public class HentUekspederForsendelseConsumer implements HentUekspederForsendelse {


	private final String administrerforsendelseV1Url;
	private final RestTemplate restTemplate;
	private static final Duration DURATION = Duration.ofMillis(300000L);

	@Inject
	public HentUekspederForsendelseConsumer(@Value("${administrerforsendelse.v1.url}") String administrerforsendelseV1Url,
											RestTemplateBuilder restTemplateBuilder, final ServiceuserAlias serviceuserAlias) {
		this.administrerforsendelseV1Url = administrerforsendelseV1Url;
		this.restTemplate = restTemplateBuilder
				.interceptors(new CallIdInterceptor())
				.setReadTimeout(DURATION)
				.setConnectTimeout(DURATION)
				.basicAuthentication(serviceuserAlias.getUsername(),serviceuserAlias.getPassword())
				.build();;
	}

	@Override
	@Retryable(include = DokDistAvstemmingTechnicalException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = "dokdist_consumer_request", extraTags = {"consumer", "DOKDIST", "process_code","hentUekspederForsendelse"}, percentiles = {0.5, 0.95})
	public List<DokDistAvstemmingForsendelse> hentUekspederForsendelse(String distribusjonKanal, Long antallTimer) {
		try {
			HttpHeaders httpHeaders = createHeaders();
			ResponseEntity<List<DokDistAvstemmingForsendelse>> responseEntity = restTemplate
					.exchange(administrerforsendelseV1Url + String.format("/hentuekspederforsendelse/%s/%s", distribusjonKanal, antallTimer),
							HttpMethod.GET, new HttpEntity<>(httpHeaders),
							new ParameterizedTypeReference<>() {
							});
			return responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			throw new DokDistAvstemmingFunctionalException(String.format("Kallet til DokumentDistribusjon  {administrerforsendelse} feilet med status=%s, feilmelding=%s",
					e.getStatusCode(), e.getMessage()), e.getStatusCode());
		} catch (HttpServerErrorException e) {
			throw new DokDistAvstemmingTechnicalException(String.format("Tjenesten DokumentDistribusjon {administrerforsendelse} feilet med status=%s, feilmedling=%s",
					e.getStatusCode(), e.getResponseBodyAsString()), e, e.getStatusCode());
		}

	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(MDCConstants.MDC_CALL_ID, MDC.get(MDCConstants.MDC_CALL_ID));
		return headers;
	}
}
