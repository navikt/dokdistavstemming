package no.nav.dokdistavstemming.sdist002;

import org.springframework.http.HttpStatus;

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
	public static final String AVSTEM_FORSENDELSER_URL = "/administrerforsendelse/avstemforsendelser";

	public static void happilyHentUekspederteForsendelser(String filename) {
		stubFor(get(urlMatching("/administrerforsendelse/hentuekspederteforsendelser/.*"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("rdist001/" + filename)));
	}

	public static void returnNoContentForHentUekspederteForsendelser() {
		stubFor(get(urlMatching("/administrerforsendelse/hentuekspederteforsendelser/.*"))
				.willReturn(aResponse().withStatus(NO_CONTENT.value())));
	}

	public static void jiraHappyOpprettSakForAvstemForsendelse() {
		stubFor(post(urlMatching(JIRA_OPPRETTE_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("jira/jiraresponse.json")));
	}

	public static void jiraHappyGetIssue() {
		stubFor(get(urlMatching(JIRA_OPPRETTE_URL + "/MMA-134"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("jira/jiraresponse.json")));
	}

	public static void jiraHappyUpdateSak(String key) {
		stubFor(post(urlMatching(JIRA_OPPRETTE_URL + "/" + key + "/transitions"))
				.willReturn(aResponse().withStatus(HttpStatus.NO_CONTENT.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	public static void jiraHappyHentProjectDetails() {
		stubFor(get(urlMatching(JIRA_MMA_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("jira/hent_project_data.json")));
	}

	public static void jiraHappyPostVedleggDokument() {
		stubFor(post(urlMatching(JIRA_VEDLEGG_URL))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("jira/lagrevedlegg-happy.json")));
	}

	public static void jiraFeilToOpprettSakForAvstemForsendelse() {
		stubFor(post(urlMatching(JIRA_OPPRETTE_URL))
				.willReturn(aResponse().withStatus(BAD_REQUEST.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("jira/jiraresponse.json")));
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

	public static void postAzureToken() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}
}
