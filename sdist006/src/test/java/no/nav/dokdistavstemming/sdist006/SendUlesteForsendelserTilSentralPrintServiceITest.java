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
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
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

	private static final String FORSENDELSE_ID = "1720847";
	private static final String NY_FORSENDELSE_ID = "33333";
	private static final String BESTILLINGSID = "811c0c5d-e74c-491a-8b8c-d94075c822c3";
	private static final String PROPERTY_BESTILLINGSID = "bestillingsId";
	private static final String JOURNALPOST_ID = "123456789";
	private static final int OK = 200;

	private static final String HENTFORSENDELSER_URL = "/rest/v1/administrerforsendelse/hentForsendelser";
	private static final String FINNULESTEFORSENDELSER_URL = "[/rest/journalpostapi/v1/journalpost/finnUlesteJournalposter/DITTNAV/].*[/].*";
	private static final String OPPDATERDISTRIBUSJONSINFO_URL = "/rest/journalpostapi/v1/journalpost/123456789/oppdaterDistribusjonsinfo";
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


	@Test
	public void shouldFeilregistrerForsendelseOgOppdaterForsendelse() throws IOException {
		stubFinnUlesteForsendelser(OK);
		stubGetHentForsendelser("__files/rdist001/hentForsendelseresponse-happy.json", JOURNALPOST_ID, OK);
		stubPostOpprettForsendelse("__files/rdist001/opprettForsendelseResponse-happy.json", OK);
		stubPutFeilregistrerforsendelse(OK);
		stubPutOppdaterForsendelse(OK);
		stuboppdaterDistribusjonsinfo(OK);


		sendUlesteForsendelserTilSentralPrintService.sendUlesteForsendelserTilSentralPrint();

		AtomicInteger recieved = new AtomicInteger();
		await().atMost(10, SECONDS).untilAsserted(() -> {
			//Sjekk at riktig forsendelseId blir sendt til qdist009/print
			String message = receive(qdist009).toString();
			System.out.println(message);
			assertThat(message).contains(NY_FORSENDELSE_ID);
		});

		verifyAndCountForsendelse(BESTILLINGSID);
	}


/*
	@Test
	public void shouldAvsluttBehandlingenWhenBestillerIdIsNotDittnavAndStatusFeilet() {
		stubGetFinnForsendelse("__files/rdist001/finnForsendelseresponse-happy.json", OK.value());
		stubGetHentForsendelse("__files/rdist001/hentForsendelseresponse-happy.json", FORSENDELSE_ID, OK.value());
		stubPostOpprettForsendelse("__files/rdist001/opprettForsendelseResponse-happy.json", OK.value());
		stubPutFeilregistrerforsendelse(OK.value());
		stubPutOppdaterForsendelse(KLAR_FOR_DIST.name(), NY_FORSENDELSE_ID, OK.value());

		sendMessageToTopic(DOKNOTIFIKASJON_STATUS_TOPIC, doknotifikasjonStatus(DOKDISTDPI, FEILET.name()));

		await().atMost(10, SECONDS).untilAsserted(() ->
				verify(0, getRequestedFor(urlEqualTo(format(FINNFORSENDELSE_URL, PROPERTY_BESTILLINGSID, BESTILLINGSID))))
		);
	}

	@Test
	public void shouldLogWhenVarselstatusIsNotEqualToOPPRETTET() {
		stubGetFinnForsendelse("__files/rdist001/finnForsendelseresponse-happy.json", OK.value());
		stubGetHentForsendelse("__files/rdist001/hentForsendelseresponse-forsendelsestatus-feilet.json", FORSENDELSE_ID, OK.value());

		sendMessageToTopic(DOKNOTIFIKASJON_STATUS_TOPIC, doknotifikasjonStatus(DOKDISTDITTNAV, FEILET.name()));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			verify(getRequestedFor(urlEqualTo(format(FINNFORSENDELSE_URL, PROPERTY_BESTILLINGSID, BESTILLINGSID))));
			verify(getRequestedFor(urlEqualTo(HENTFORSENDELSE_URL)));
		});
	}

	@Test
	public void shouldUpdateDistInfo() {
		stubGetFinnForsendelse("__files/rdist001/finnForsendelseresponse-happy.json", OK.value());
		stubNotifikasjonInfo("__files/rnot001/doknot-happy.json", OK.value());
		stubUpdateVarselInfo();
		stubGetHentForsendelse("__files/rdist001/hentForsendelseresponse-happy.json", FORSENDELSE_ID, OK.value());
		stubPutOppdaterForsendelse(EKSPEDERT.name(), FORSENDELSE_ID, OK.value());

		sendMessageToTopic(DOKNOTIFIKASJON_STATUS_TOPIC, doknotifikasjonStatus(DOKDISTDITTNAV, OVERSENDT.name(), null));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			verify(1, getRequestedFor(urlEqualTo(format(FINNFORSENDELSE_URL, PROPERTY_BESTILLINGSID, BESTILLINGSID))));
			verify(1, putRequestedFor((urlEqualTo(OPPDATERVARSELINFO_URL))));
		});
	}

	@Test
	public void shouldLogAndAvsluttBehandlingHvisForsendelseStatusErFEILET() {
		stubGetFinnForsendelse("__files/rdist001/finnForsendelseresponse-happy.json", OK.value());
		stubGetHentForsendelse("__files/rdist001/hentForsendelseresponse-forsendelsestatus-feilet.json", FORSENDELSE_ID, OK.value());

		sendMessageToTopic(DOKNOTIFIKASJON_STATUS_TOPIC, doknotifikasjonStatus(DOKDISTDITTNAV, FEILET.name()));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			ConsumerRecord<String, Object> record = records.poll();
			assertTrue(record != null);
			assertTrue(record.value().toString().contains(MELDING));
			verify(getRequestedFor(urlEqualTo(format(FINNFORSENDELSE_URL, PROPERTY_BESTILLINGSID, BESTILLINGSID))));
			verify(getRequestedFor(urlEqualTo(HENTFORSENDELSE_URL)));
		});
	}*/

	private void verifyAndCountForsendelse(String bestillingsId) {
		verify(getRequestedFor(urlPathMatching((FINNULESTEFORSENDELSER_URL))));
		verify(getRequestedFor(urlEqualTo(HENTFORSENDELSER_URL)));
		verify(postRequestedFor(urlMatching("/rest/v1/administrerforsendelse")));
		verify(putRequestedFor(urlMatching(FEILREGISTRERFORSENDELSE_URL)));
		verify(putRequestedFor(urlEqualTo(OPPDATERFORSENDELSE_URL)));
		verify(postRequestedFor(urlEqualTo(OPPDATERDISTRIBUSJONSINFO_URL)));
	}

	private void stubFinnUlesteForsendelser(int httpStatusValue) {
		stubFor(post(urlPathMatching(FINNULESTEFORSENDELSER_URL))
				.willReturn(aResponse()
						.withStatus(httpStatusValue)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody("[123456789]")));
	}

	private void stubGetHentForsendelser(String responsebody, String journalpostId, int httpStatusvalue) throws IOException {
		stubFor(get(HENTFORSENDELSER_URL)
				.withRequestBody(containing(journalpostId))
				.willReturn(aResponse()
						.withStatus(httpStatusvalue)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString(responsebody))));
	}

	private void stubPutOppdaterForsendelse(int httpStatusvalue) {
		stubFor(put(OPPDATERFORSENDELSE_URL)
				.willReturn(aResponse()
						.withStatus(httpStatusvalue)));
	}

	private void stubPutFeilregistrerforsendelse(int httpStatusValue) {
		stubFor(put(FEILREGISTRERFORSENDELSE_URL)
				.willReturn(aResponse()
						.withStatus(httpStatusValue)));
	}

	private void stubPostOpprettForsendelse(String responseBody, int httpStatusValue) throws IOException {
		stubFor(post(urlEqualTo("/rest/v1/administrerforsendelse"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(httpStatusValue)
						.withBody(classpathToString(responseBody))));
	}

	private void stuboppdaterDistribusjonsinfo(int httpStatusValue) {
		stubFor(post(OPPDATERDISTRIBUSJONSINFO_URL)
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(httpStatusValue)));
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