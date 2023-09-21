package no.nav.dokdistavstemming.sdist006;

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import no.nav.dokdistavstemming.SendUlesteForsendelserTilSentralPrintService;
import no.nav.dokdistavstemming.config.ApplicationTestConfig;
import no.nav.doknotifikasjon.schemas.DoknotifikasjonStopp;
import org.apache.http.HttpHeaders;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeTypeUtils;

import javax.jms.Queue;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
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
import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.dokdistavstemming.sdist006.SendUlesteForsendelserTilSentralPrintServiceITest.RENOTIFIKASJON_STOPP_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EmbeddedKafka(
		topics = {RENOTIFIKASJON_STOPP_TOPIC},
		bootstrapServersProperty = "spring.kafka.bootstrap-servers",
		partitions = 1
)
@ActiveProfiles("itest")
class SendUlesteForsendelserTilSentralPrintServiceITest extends ApplicationTestConfig {

	private static final int OK = 200;
	private static final String NY_FORSENDELSE_ID1 = "33333";
	private static final String NY_FORSENDELSE_ID2 = "44444";
	private static final String OLD_FORSENDELSEID1 = "987654321";
	private static final String OLD_FORSENDELSEID2 = "287654321";
	private static final String JOURNALPOSTID1 = "123456789";
	private static final String JOURNALPOSTID2 = "999654321";
	private static final String JOURNALPOSTLISTE = "[" + JOURNALPOSTID1 + "," + JOURNALPOSTID2 + "]";
	private static final String DOKDISTDITTNAV = "dokdistdittnav";
	public static final String RENOTIFIKASJON_STOPP_TOPIC = "teamdokumenthandtering.privat-dok-notifikasjon-stopp";
	private static final String GAMMEL_BESTILLINGSID1 = "811c0c5d-e74c-491a-8b8c-d94075c822c3";
	private static final String GAMMEL_BESTILLINGSID2 = "811c0c5d-e74c-491a-8b8c-dette-er-en-annen";

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


	public static Consumer<String, DoknotifikasjonStopp> consumer;

	@Autowired
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	public EmbeddedKafkaBroker kafkaEmbedded;

	@BeforeEach
	public void setUpClass() {
		// KafkaConsumer for å kunne konsumere meldinger som InngaaendeHendelsePublisher dytter til 'test-ut-topic'
		this.setUpConsumerForTopicUt();
	}


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
		stubGetFinnUlesteForsendelser(JOURNALPOSTLISTE);
		stubGetHentForsendelser("__files/rdist001/hentForsendelseresponse-happy.json");
		stubPostOpprettForsendelse("__files/rdist001/opprettForsendelseResponse1-happy.json", GAMMEL_BESTILLINGSID1);
		stubPostOpprettForsendelse("__files/rdist001/opprettForsendelseResponse2-happy.json", GAMMEL_BESTILLINGSID2);
		stubPutFeilregistrerforsendelse(OLD_FORSENDELSEID1);
		stubPutFeilregistrerforsendelse(OLD_FORSENDELSEID2);
		stubPutOppdaterForsendelse(NY_FORSENDELSE_ID1);
		stubPutOppdaterForsendelse(NY_FORSENDELSE_ID2);
		stubPatchOppdaterDistribusjonsinfo();


		sendUlesteForsendelserTilSentralPrintService.sendUlesteForsendelserTilSentralPrint();

		await().atMost(10, SECONDS).untilAsserted(() -> {
			//Sjekk at riktig forsendelseId blir sendt til qdist009/print
			String message = receive(qdist009).toString();
			assertThat(message).contains(NY_FORSENDELSE_ID1);
			String message2 = receive(qdist009).toString();
			assertThat(message2).contains(NY_FORSENDELSE_ID2);

			List<DoknotifikasjonStopp> records = this.getAllCurrentRecordsOnTopicRenotifikasjonStopp();
			assertEquals(2, records.size());
			assertRecord(records.get(0), GAMMEL_BESTILLINGSID1);
			assertRecord(records.get(1), GAMMEL_BESTILLINGSID2);
		});

		verifyAndCountForsendelse();
	}

	private void assertRecord(DoknotifikasjonStopp doknotifikasjonStopp, String bestillingsId) {
		assertEquals(bestillingsId, doknotifikasjonStopp.getBestillingsId());
		assertEquals(DOKDISTDITTNAV, doknotifikasjonStopp.getBestillerId());
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
		stubGetFinnUlesteForsendelser(JOURNALPOSTLISTE);
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
				.withQueryParam("journalpostliste", equalTo(JOURNALPOSTID1))
				.withQueryParam("journalpostliste", equalTo(JOURNALPOSTID2))
				.willReturn(aResponse()
						.withStatus(OK)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString(responsebody))));
	}

	private void stubPutOppdaterForsendelse(String forsendelsesId) {
		stubFor(put(OPPDATERFORSENDELSE_URL)
				.withRequestBody(containing("\"forsendelseId\":" + forsendelsesId))
				.withRequestBody(containing("\"forsendelseStatus\":\"KLAR_FOR_DIST\""))
				.willReturn(aResponse()
						.withStatus(OK)));
	}

	private void stubPutFeilregistrerforsendelse(String forsendelsesId) {
		stubFor(put(FEILREGISTRERFORSENDELSE_URL)
				.withRequestBody(containing("\"forsendelseId\":" + forsendelsesId))
				.withRequestBody(containing("\"feilTypeCode\":\"MELDINGSFEIL\""))
				.withRequestBody(containing("\"detaljer\":\"Forsendelse til NAV.NO er ikke lest innen frist.\""))
				.willReturn(aResponse()
						.withStatus(OK)));
	}

	private void stubPostOpprettForsendelse(String responseBody, String oldBestillingsId) throws IOException {
		stubFor(post(urlEqualTo("/rest/v1/administrerforsendelse"))
				.withRequestBody(containing("\"originalDistribusjonId\":" + "\"" + oldBestillingsId + "\""))
				.withRequestBody(containing("\"distribusjonsKanal\":\"PRINT\""))
				.withRequestBody(containing("\"dokumenttypeId\":\"U000001\""))
				.withRequestBody(containing("\"bestillingsId\":" + anyString()))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(OK)
						.withBody(classpathToString(responseBody))));
	}

	private void stubPatchOppdaterDistribusjonsinfo() {
		stubFor(patch(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL))
				.withRequestBody(containing("\"settStatusEkspedert\":false"))
				.withRequestBody(containing("\"utsendingsKanal\":\"S\""))
				.withRequestBody(containing("\"tilbakestillJournalpost\":true"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(OK)));
	}

	@SuppressWarnings("unchecked")
	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement<?>) response).getValue();
		}
		return (T) response;
	}

	public List<DoknotifikasjonStopp> getAllCurrentRecordsOnTopicRenotifikasjonStopp() {
		return StreamSupport.stream(KafkaTestUtils.getRecords(consumer, ofSeconds(2).toMillis()).records(RENOTIFIKASJON_STOPP_TOPIC).spliterator(), false)
				.map(ConsumerRecord::value)
				.collect(Collectors.toList());
	}

	public void setUpConsumerForTopicUt() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test", "true", kafkaEmbedded);
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
		consumerProps.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://localhost");
		consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

		consumer = new DefaultKafkaConsumerFactory<String, DoknotifikasjonStopp>(consumerProps).createConsumer();
		consumer.subscribe(singletonList(RENOTIFIKASJON_STOPP_TOPIC));
	}

	public static String classpathToString(String path) throws IOException {
		InputStream inputStream = new ClassPathResource(path).getInputStream();
		String message = new String(inputStream.readAllBytes(), UTF_8);
		IOUtils.closeQuietly(inputStream);
		return message;
	}
}