package no.nav.dokdistavstemming.utils;

import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class WireMockResponse {

    public static final String EKSPEDERTEFORSENDELSER_URL = "/administrerforsendelse/hentekspederteforsendelser";
    public static final String AVSTEMFORSENDELSE_URL = "/administrerforsendelse/avstemekspederteforsendelser";
    public static final String JOURNALPOST_API_URL = "/bulkOppdaterDistribusjonsinfo";

    public static void getEkspederteForsendelser() throws Exception {
        stubFor(get(urlMatching(EKSPEDERTEFORSENDELSER_URL))
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(classpathToString("__files/rdist001/ekspedertforsendelse.json"))));
    }


    public static void oppdaterAvstemArkivFrosendelseInfo() {
        stubFor(put(urlMatching(AVSTEMFORSENDELSE_URL))
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }

    public static void oppdaterJournalpost() {
        stubFor(post(urlMatching(JOURNALPOST_API_URL))
                .willReturn(aResponse().withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }

    public static void oppdaterAvstemFrosendelseInfoFeilWithInternalServerError() {
        stubFor(put(urlMatching(EKSPEDERTEFORSENDELSER_URL))
                .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }

    public static String classpathToString(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);
    }

}
