package no.nav.dokdistavstemming.utils;

import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class WireMockResponse {

	public static final String JIRA_OPPRETTE_URL = "/rest/api/2/issue";
	public static final String JIRA_VEDLEGG_URL = "/rest/api/2/issue/MMA-134/attachments";
	public static final String JIRA_MMA_URL = "/rest/api/2/project/MMA";
	public static final String HENT_EKSPEDERTE_FORSENDELSER_URL = "/administrerforsendelse/hentekspederteforsendelser";
	public static final String AVSTEM_FORSENDELSER_URL = "/administrerforsendelse/avstemforsendelser";
	public static final String AVSTEM_EKSPEDERTE_FORSENDELSER_URL = "/administrerforsendelse/avstemekspederteforsendelser";
	public static final String JOURNALPOST_API_URL = "/rest/journalpostapi/bulkOppdaterDistribusjonsinfo";

	public static void happilyHentUekspederteForsendelser(String filePath) throws Exception {
		stubFor(get(urlMatching("/administrerforsendelse/hentuekspederteforsendelser/.*"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(TestUtils.classpathToString(filePath))));
	}

	public static void returnNoContentForHentUekspederteForsendelser() {
		stubFor(get(urlMatching("/administrerforsendelse/hentuekspederteforsendelser/.*"))
				.willReturn(aResponse().withStatus(NO_CONTENT.value())));
	}

	public static void jiraHappyOpprettSakForAvstemForsendelse() throws Exception {
		stubFor(post(urlMatching(JIRA_OPPRETTE_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(TestUtils.classpathToString("__files/jira/jiraresponse.json"))));
	}

	public static void jiraHappyGetIssue() throws Exception {
		stubFor(get(urlMatching(JIRA_OPPRETTE_URL + "/MMA-134"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(TestUtils.classpathToString("__files/jira/jiraresponse.json"))));
	}

	public static void jiraHappyUpdateSak(String key) {
		stubFor(post(urlMatching(JIRA_OPPRETTE_URL + "/" + key + "/transitions"))
				.willReturn(aResponse().withStatus(HttpStatus.NO_CONTENT.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	public static void jiraHappyHentProjectDetails() throws Exception {
		stubFor(get(urlMatching(JIRA_MMA_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(TestUtils.classpathToString("__files/jira/hent_project_data.json"))));
	}

	public static void jiraHappyPostVedleggDokument() throws Exception {
		stubFor(post(urlMatching(JIRA_VEDLEGG_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(TestUtils.classpathToString("__files/jira/laggevedlagg-happy-return.json"))));
	}

	public static void jiraFeilToOpprettSakForAvstemForsendelse() throws Exception {
		stubFor(post(urlMatching(JIRA_OPPRETTE_URL))
				.willReturn(aResponse().withStatus(BAD_REQUEST.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(TestUtils.classpathToString("__files/jira/jiraresponse.json"))));
	}

	public static void oppdaterAvstemForsendelseInfo() {
		stubFor(put(urlMatching(AVSTEM_FORSENDELSER_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	public static void oppdaterAvstemForsendelsesinfoFeil() {
		stubFor(put(urlMatching(AVSTEM_FORSENDELSER_URL))
				.willReturn(aResponse().withStatus(BAD_REQUEST.value())));
	}

	public static void oppdaterAvstemForsendelseInfoFeilWithInternalServerError() {
		stubFor(put(urlMatching(AVSTEM_FORSENDELSER_URL))
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	public static void getEkspederteForsendelser(String filePath) throws Exception {
		stubFor(get(urlMatching(HENT_EKSPEDERTE_FORSENDELSER_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(TestUtils.classpathToString(filePath))));
	}

	public static void oppdaterAvstemArkivForsendelseInfo() {
		stubFor(put(urlMatching(AVSTEM_EKSPEDERTE_FORSENDELSER_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	public static void oppdaterJournalpost(String path) throws IOException {
		stubFor(post(urlMatching(JOURNALPOST_API_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(TestUtils.classpathToString(path))));
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
