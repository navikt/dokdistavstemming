package no.nav.dokdistavstemming.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static no.nav.dokdistavstemming.utils.TestUtils.classpathToString;

public class WireMockResponse {

	public static void dokDistHappyHentUekspedereFrosendelse() throws Exception {
		stubFor(get(urlMatching("/administrerforsendelse/henteuekspederforsendelse/(.*?)/(.*?)"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/rdist001/hentForsendelse-SDP-SixTime.json"))));
	}

	public static void dokDistHappyHentEmptyUekspedereFrosendelse() throws Exception {
		stubFor(get(urlMatching("/administrerforsendelse/henteuekspederforsendelse/(.*?)/(.*?)"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/rdist001/hentuekspedereforsendelse-empty.json"))));
	}

	public static void happilyHentForsendelseKvitteringIkkeMottattKanalPrint() throws Exception {
		stubFor(get(urlMatching("/administrerforsendelse/henteuekspederforsendelse/(.*?)/(.*?)"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/rdist001/henteforsendelse-print-overfemdager.json"))));
	}


	public static void jiraHappyOpprettSakForAvstemFrosendelse() throws Exception{
		stubFor(post(urlMatching("/rest/api/2/issue"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/jira/jiraresponse.json"))));
	}

	public static void jiraHappyHentProjectDetails() throws Exception {
		stubFor(get(urlMatching("/rest/api/2/project/MMA"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/jira/hent_project_data.json"))));
	}

	public static void jiraHappyPostVedleggDokument() throws Exception{
		stubFor(post(urlMatching("/rest/api/2/issue/MMA-134/attachments"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/jira/laggevedlagg-happy-return.json"))));
	}

	public static void jiraFeilToOpprettSakForAvstemFrosendelse() throws Exception{
		stubFor(post(urlMatching("/rest/api/2/issue"))
				.willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
						.withHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/jira/jiraresponse.json"))));
	}


	public static void oppdaterAvstemFrosendelseInfo() throws Exception{
		stubFor(put(urlMatching("/administrerforsendelse/avstemforsendelser"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)));
	}

	public static void oppdaterAvstemFrosendelseInfoFeil() throws Exception{
		stubFor(put(urlMatching("/administrerforsendelse/avstemforsendelser"))
				.willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())));
	}

	public static void oppdaterAvstemFrosendelseInfoFeilWithInternalServerError() throws Exception{
		stubFor(put(urlMatching("/administrerforsendelse/avstemforsendelser"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)));
	}


}
