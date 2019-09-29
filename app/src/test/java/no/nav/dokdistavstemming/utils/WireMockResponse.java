package no.nav.dokdistavstemming.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static no.nav.dokdistavstemming.utils.TestUtils.classpathToString;

public class WireMockResponse {

	public static void dokDistHappyHentUekspedereFrosendelse() throws Exception {
		stubFor(get(urlMatching("/administrerforsendelse/henteuekspederforsendelse/(.*?)/(.*?)"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/hentForsendelse-SDP-SixTime.json"))));
	}

	public static void dokDistHappyHentEmptyUekspedereFrosendelse() throws Exception {
		stubFor(get(urlMatching("/administrerforsendelse/henteuekspederforsendelse/(.*?)/(.*?)"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/hentuekspedereforsendelse-empty.json"))));
	}

	public static void dokDistHappyHentUekspedereFrosendelseKanalPrint() throws Exception {
		stubFor(get(urlMatching("/administrerforsendelse/henteuekspederforsendelse/(.*?)/(.*?)"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/henteforsendelse-print-overfemdager.json"))));
	}


	public static void dokDistHappyOppretteJiraSak() throws Exception{
		stubFor(post(urlMatching("/rest/api/2/issue"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
						.withBody("{response: ok}")));
	}


}
