package no.nav.dokdistavstemming;

import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class WireMockResponse {

	public static final String HENT_EKSPEDERTE_FORSENDELSER_URL = "/administrerforsendelse/hentekspederteforsendelser";
	public static final String AVSTEM_EKSPEDERTE_FORSENDELSER_URL = "/administrerforsendelse/avstemekspederteforsendelser";
	public static final String JOURNALPOST_API_URL = "/rest/journalpostapi/v1/bulkOppdaterDistribusjonsinfo";

	public static void getEkspederteForsendelser(String filename) {
		stubFor(get(urlMatching(HENT_EKSPEDERTE_FORSENDELSER_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("rdist001/" + filename)));
	}

	public static void oppdaterAvstemArkivForsendelseInfo() {
		stubFor(put(urlMatching(AVSTEM_EKSPEDERTE_FORSENDELSER_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	public static void oppdaterJournalpost(String filename) {
		stubFor(post(urlMatching(JOURNALPOST_API_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("journalpost/" + filename)));
	}

	public static void oppdaterJournalpostFeil(HttpStatus status) {
		stubFor(post(urlMatching(JOURNALPOST_API_URL))
				.willReturn(aResponse().withStatus(status.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	public static void postAzureToken() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

}
