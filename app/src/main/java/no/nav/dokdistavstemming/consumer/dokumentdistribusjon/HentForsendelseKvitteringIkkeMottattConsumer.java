package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseTechnicalException;
import no.nav.dokdistavstemming.mdc.MDCConstants;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class HentForsendelseKvitteringIkkeMottattConsumer implements HentForsendelseKvitteringIkkeMottatt {

    private final String administrerforsendelseV1Url;
    private final RestTemplate restTemplate;

    @Autowired
    public HentForsendelseKvitteringIkkeMottattConsumer(@Value("${administrerforsendelse.v1.url}") String administrerforsendelseV1Url,
                                                        RestTemplate restTemplate) {
        this.administrerforsendelseV1Url = administrerforsendelseV1Url;
        this.restTemplate = restTemplate;
    }

    @Override
    @Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    @Monitor(value = "dokdist_consumer_request", extraTags = {"consumer", "DOKDIST", "process_code", "hentForsendelserKvitteringIkkeMottatt"}, percentiles = {0.5, 0.95})
    public List<AvstemForsendelseRequestTo> hentForsendelserKvitteringIkkeMottatt(String distribusjonKanal, int antallTimer) {
        MDC.put(MDCConstants.MDC_CONSUMER_ID, "hentForsendelserKvitteringIkkeMottatt");
        try {
            HttpHeaders httpHeaders = createHeaders();
            log.info("{} har mottatt kall om å hente forsendelser fra rdist001(dokdist) med distribusjonKanal={}, antallTimer={}",
                    MDC.get(MDCConstants.MDC_CONSUMER_ID), distribusjonKanal, antallTimer);
            ResponseEntity<AvstemForsendelseRequestTo[]> responseEntity = restTemplate
                    .exchange(String.format("%s/henteuekspederforsendelse/%s/%s", administrerforsendelseV1Url, distribusjonKanal, antallTimer),
                            HttpMethod.GET, new HttpEntity<>(httpHeaders),  AvstemForsendelseRequestTo[].class );

            return responseEntity.getBody() == null ? Collections.emptyList() : Arrays.asList(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            log.warn("{} Kall mot rdist001 feilet med status={}, feilmelding={}", MDC.get(MDCConstants.MDC_CONSUMER_ID), e.getStatusCode(), e.getMessage());
            throw new AvstemForsendelseFunctionalException(
                    String.format("Kall mot rdist001 feilet med status=%s, feilmelding=%s", e.getStatusCode(), e.getMessage()), e.getStatusCode()
            );
        } catch (HttpServerErrorException e) {
            log.warn("Kall mot rdist001 feilet teknisk. status={}, feilmelding={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AvstemForsendelseTechnicalException(
                    String.format("Kall mot rdist001 feilet teknisk. status=%s, feilmelding=%s", e.getStatusCode(), e.getResponseBodyAsString()), e, e.getStatusCode()
            );
        }
    }

    @Override
    @Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = 500, multiplier = 2))
    @Monitor(value = "dokdist_consumer_request", extraTags = {"consumer", "DOKDIST", "process_code", "oppdaterForsendelserAvstemDatoOgReferanse"}, percentiles = {0.5, 0.95})
    public void oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {

        try {
            HttpEntity<OppdaterForsendelserAvstemtInfo> httpEntity = new HttpEntity<>(oppdaterForsendelserAvstemtInfo, createHeaders());
            log.info("{} har mottatt kall om å oppdatere forsendelser fra rdist001 med avstemtReferanse={}",
                    MDC.get(MDCConstants.MDC_CONSUMER_ID), oppdaterForsendelserAvstemtInfo.getAvstemtReferanse());
            restTemplate.exchange(administrerforsendelseV1Url + "/avstemforsendelser", HttpMethod.PUT, httpEntity, Object.class);
            log.info("Forsendelser med forsendelseIder={} oppdatert", oppdaterForsendelserAvstemtInfo.getForsendelser());
        } catch (HttpClientErrorException e) {
            log.warn("Kall mot rdist001 feilet med status={}, feilmelding={}", e.getStatusCode(), e.getMessage());
            throw new AvstemForsendelseFunctionalException(
                    String.format("Kall mot rdist001 feilet med status=%s, feilmelding=%s", e.getStatusCode(), e.getMessage()), e.getStatusCode()
            );
        } catch (HttpServerErrorException e) {
            log.warn("Kall mot rdist001 feilet teknisk. status={}, feilmelding={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AvstemForsendelseTechnicalException(
                    String.format("Kall mot rdist001 feilet teknisk. status=%s, feilmelding=%s", e.getStatusCode(), e.getResponseBodyAsString()), e, e.getStatusCode()
            );
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(MDCConstants.MDC_CALL_ID, MDC.get(MDCConstants.MDC_CALL_ID));
        return headers;
    }
}
