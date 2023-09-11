package no.nav.dokdistavstemming.sdist006;

import no.nav.dokdistavstemming.SendUlesteForsendelserTilSentralPrintService;
import no.nav.dokdistavstemming.config.ApplicationTestConfig;
import org.apache.http.HttpHeaders;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeTypeUtils;

import javax.jms.Queue;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ActiveProfiles("itest")
class SendUlesteForsendelserTilSentralPrintServiceITest extends ApplicationTestConfig {

	private static final String NY_FORSENDELSE_ID = "33333";
	private static final int OK = 200;

	private static final String HENTFORSENDELSER_URL = "/rest/v1/administrerforsendelse/hentForsendelser.*";
	private static final String FINNULESTEFORSENDELSER_URL = "/rest/internal/sikkerhetsnivaa/finnUlesteJournalposter/NAV_NO/202[\\d]-.*";
	private static final String OPPDATERDISTRIBUSJONSINFO_URL = "/rest/journalpostapi/v1/journalpost/.*/oppdaterDistribusjonsinfo";
	private static final String OPPDATERFORSENDELSE_URL = "/rest/v1/administrerforsendelse/oppdaterforsendelse";
	private static final String FEILREGISTRERFORSENDELSE_URL = "/rest/v1/administrerforsendelse/feilregistrerforsendelse";

	@Autowired
	private Queue qdist009;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService;


	@BeforeEach
	void setUp() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	//@Test
	//TODO: Enable denne etter at jeg har verifisert at mq-config for prod er riktig (får sendt en melding med dummy-innhold til qdist009)
	public void shouldFeilregistrerForsendelseOgOppdaterForsendelse() throws IOException {
		stubGetFinnUlesteForsendelser("[123456789,987654321]");
		stubGetHentForsendelser("__files/rdist001/hentForsendelseresponse-happy.json");
		stubPostOpprettForsendelse("__files/rdist001/opprettForsendelseResponse-happy.json");
		stubPutFeilregistrerforsendelse();
		stubPutOppdaterForsendelse();
		stubPatchOppdaterDistribusjonsinfo();


		sendUlesteForsendelserTilSentralPrintService.sendUlesteForsendelserTilSentralPrint();

		await().atMost(10, SECONDS).untilAsserted(() -> {
			//Sjekk at riktig forsendelseId blir sendt til qdist009/print
			String message = receive(qdist009).toString();
			assertThat(message).contains(NY_FORSENDELSE_ID);
			String message2 = receive(qdist009).toString();
			assertThat(message2).contains(NY_FORSENDELSE_ID);
		});

		verifyAndCountForsendelse();
	}

	@Test
	public void shouldStopWhenNoJournalpostsFromDokarkiv() {
		stubGetFinnUlesteForsendelser("[]");

		sendUlesteForsendelserTilSentralPrintService.sendUlesteForsendelserTilSentralPrint();

		verify(exactly(1), getRequestedFor(urlPathMatching((FINNULESTEFORSENDELSER_URL))));
		verify(exactly(0), getRequestedFor(urlPathMatching(HENTFORSENDELSER_URL)));
	}

	@Test
	public void shouldStopWhenNoForsendelserFromDokdistadmin() throws IOException {
		stubGetFinnUlesteForsendelser("[123456789]");
		stubGetHentForsendelser("__files/rdist001/hentForsendelseresponse-empty.json");

		sendUlesteForsendelserTilSentralPrintService.sendUlesteForsendelserTilSentralPrint();

		verify(exactly(1), getRequestedFor(urlPathMatching((FINNULESTEFORSENDELSER_URL))));
		verify(exactly(1), getRequestedFor(urlPathMatching(HENTFORSENDELSER_URL)));
		verify(exactly(0), postRequestedFor(urlMatching("/rest/v1/administrerforsendelse")));
		verify(exactly(0), putRequestedFor(urlMatching(FEILREGISTRERFORSENDELSE_URL)));
		verify(exactly(0), putRequestedFor(urlEqualTo(OPPDATERFORSENDELSE_URL)));
		verify(exactly(0), patchRequestedFor(urlMatching(OPPDATERDISTRIBUSJONSINFO_URL)));
	}

	private void verifyAndCountForsendelse() {
		verify(exactly(1), getRequestedFor(urlPathMatching((FINNULESTEFORSENDELSER_URL))));
		verify(exactly(1), getRequestedFor(urlPathMatching(HENTFORSENDELSER_URL)));
		verify(exactly(2), postRequestedFor(urlMatching("/rest/v1/administrerforsendelse")));
		verify(exactly(2), putRequestedFor(urlMatching(FEILREGISTRERFORSENDELSE_URL)));
		verify(exactly(2), putRequestedFor(urlEqualTo(OPPDATERFORSENDELSE_URL)));
		verify(exactly(2), patchRequestedFor(urlMatching(OPPDATERDISTRIBUSJONSINFO_URL)));
	}

	private void stubGetFinnUlesteForsendelser(String journalpostListe) {
		stubFor(get(urlPathMatching(FINNULESTEFORSENDELSER_URL))
				.willReturn(aResponse()
						.withStatus(OK)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(journalpostListe)));
	}

	private void stubGetHentForsendelser(String responsebody) throws IOException {
		stubFor(get(urlPathMatching(HENTFORSENDELSER_URL))
				.willReturn(aResponse()
						.withStatus(OK)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString(responsebody))));
	}

	private void stubPutOppdaterForsendelse() {
		stubFor(put(OPPDATERFORSENDELSE_URL)
				.willReturn(aResponse()
						.withStatus(OK)));
	}

	private void stubPutFeilregistrerforsendelse() {
		stubFor(put(FEILREGISTRERFORSENDELSE_URL)
				.willReturn(aResponse()
						.withStatus(OK)));
	}

	private void stubPostOpprettForsendelse(String responseBody) throws IOException {
		stubFor(post(urlEqualTo("/rest/v1/administrerforsendelse"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(OK)
						.withBody(classpathToString(responseBody))));
	}

	private void stubPatchOppdaterDistribusjonsinfo() {
		stubFor(patch(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(OK)));
	}

	@SuppressWarnings("unchecked")
	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

	public static String classpathToString(String path) throws IOException {
		InputStream inputStream = new ClassPathResource(path).getInputStream();
		String message = new String(inputStream.readAllBytes(), UTF_8);
		IOUtils.closeQuietly(inputStream);
		return message;
	}
}