package no.nav.dokdistavstemming.sdist006;

import jakarta.jms.Queue;
import jakarta.xml.bind.JAXBElement;
import no.nav.dokdistavstemming.config.ApplicationTestConfig;
import no.nav.doknotifikasjon.schemas.DoknotifikasjonStopp;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static no.nav.dokdistavstemming.sdist006.SendUlesteForsendelserTilSentralPrintServiceITest.RENOTIFIKASJON_STOPP_TOPIC;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_INSTANCE_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;

@EmbeddedKafka(
		topics = {RENOTIFIKASJON_STOPP_TOPIC},
		partitions = 1
)
@SpringBootTest(
		classes = {
				ApplicationTestConfig.class
		},
		webEnvironment = RANDOM_PORT
)
@EnableWireMock
@ActiveProfiles("itest")
class SendUlesteForsendelserTilSentralPrintServiceITest {

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
	private static final String FINNULESTEFORSENDELSER_URL = "/rest/internal/finnUlesteJournalposter/NAV_NO/202[\\d]-.*";
	private static final String OPPDATERDISTRIBUSJONSINFO_URL = "/rest/journalpostapi/v1/journalpost/.*/oppdaterDistribusjonsinfo";
	private static final String OPPDATERFORSENDELSE_URL = "/rest/v1/administrerforsendelse/oppdaterforsendelse";
	private static final String FEILREGISTRERFORSENDELSE_URL = "/rest/v1/administrerforsendelse/feilregistrerforsendelse";

	@Autowired
	private Queue qdist009;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService;

	@Autowired
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	public EmbeddedKafkaBroker kafkaEmbedded;

	@BeforeEach
	void setUp() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
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

		//Sjekk at riktig forsendelseId blir sendt til qdist009/print
		String message = receive(qdist009).toString();
		assertThat(message).contains(NY_FORSENDELSE_ID1);
		String message2 = receive(qdist009).toString();
		assertThat(message2).contains(NY_FORSENDELSE_ID2);

		List<DoknotifikasjonStopp> records = this.getAllCurrentRecordsOnTopicRenotifikasjonStopp();
		assertEquals(2, records.size());
		assertRecord(records.get(0), GAMMEL_BESTILLINGSID1);
		assertRecord(records.get(1), GAMMEL_BESTILLINGSID2);

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

	@Test
	public void shouldSkipForsendelseWithMoreThan100Vedlegg() {
		// 102 documents = 101 vedlegg + 1 main (size() > 101 → skipped entirely)
		stubGetFinnUlesteForsendelser("[" + JOURNALPOSTID1 + "]");
		stubFor(get(urlPathMatching(HENTFORSENDELSER_URL))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(forsendelseResponseWithDokumenter(OLD_FORSENDELSEID1, JOURNALPOSTID1, GAMMEL_BESTILLINGSID1, 102))));

		sendUlesteForsendelserTilSentralPrintService.sendUlesteForsendelserTilSentralPrint();

		verify(exactly(0), postRequestedFor(urlMatching("/rest/v1/administrerforsendelse")));
		verify(exactly(0), putRequestedFor(urlMatching(FEILREGISTRERFORSENDELSE_URL)));
		verify(exactly(0), putRequestedFor(urlEqualTo(OPPDATERFORSENDELSE_URL)));
		verify(exactly(0), patchRequestedFor(urlMatching(OPPDATERDISTRIBUSJONSINFO_URL)));
	}

	@Test
	public void shouldProcessForsendelseWithExactly100Vedlegg() throws IOException {
		// 101 total documents = 100 vedlegg + 1 main (size() == 101, NOT > 101 → processed)
		stubGetFinnUlesteForsendelser("[" + JOURNALPOSTID1 + "]");
		stubFor(get(urlPathMatching(HENTFORSENDELSER_URL))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(forsendelseResponseWithDokumenter(OLD_FORSENDELSEID1, JOURNALPOSTID1, GAMMEL_BESTILLINGSID1, 101))));
		stubPostOpprettForsendelse("__files/rdist001/opprettForsendelseResponse1-happy.json", GAMMEL_BESTILLINGSID1);
		stubPutFeilregistrerforsendelse(OLD_FORSENDELSEID1);
		stubPutOppdaterForsendelse(NY_FORSENDELSE_ID1);
		stubPatchOppdaterDistribusjonsinfo();

		sendUlesteForsendelserTilSentralPrintService.sendUlesteForsendelserTilSentralPrint();

		verify(exactly(1), postRequestedFor(urlMatching("/rest/v1/administrerforsendelse")));
		verify(exactly(1), putRequestedFor(urlMatching(FEILREGISTRERFORSENDELSE_URL)));
		verify(exactly(1), putRequestedFor(urlEqualTo(OPPDATERFORSENDELSE_URL)));
		verify(exactly(1), patchRequestedFor(urlMatching(OPPDATERDISTRIBUSJONSINFO_URL)));
	}

	private static String forsendelseResponseWithDokumenter(String forsendelseId, String journalpostId, String bestillingsId, int antallDokumenter) {
		String dokumenter = IntStream.range(0, antallDokumenter)
				.mapToObj(i -> """
						{"tilknyttetSom":"%s","dokumentObjektReferanse":"testKey%d","arkivDokumentInfoId":"%d","dokumenttypeId":"U000001"}"""
						.formatted(i == 0 ? "HOVEDDOKUMENT" : "VEDLEGG", i, 1000 + i))
				.collect(Collectors.joining(","));

		return """
				{"forsendelseListe":[{"forsendelseId":"%s","bestillingsId":"%s","distribusjonKanal":"DITTNAV","forsendelseStatus":"OVERSENDT","tema":"FS22","forsendelseTittel":"Tittel","mottaker":{"mottakerId":"22222222222","mottakerNavn":"TEST PERSON","mottakerType":"PERSON"},"arkivInformasjon":{"arkivSystem":"JOARK","arkivId":"%s"},"postadresse":{"adresselinje1":"Adresslinje 1","postnummer":"1111","poststed":"Oslo","landkode":"NO"},"dokumenter":[%s]}]}"""
				.formatted(forsendelseId, bestillingsId, journalpostId, dokumenter);
	}

	private void stubGetFinnUlesteForsendelser(String journalpostListe) {
		stubFor(get(urlPathMatching(FINNULESTEFORSENDELSER_URL))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(journalpostListe)));
	}

	private void stubGetHentForsendelser(String responsebody) throws IOException {
		stubFor(get(urlPathMatching(HENTFORSENDELSER_URL))
				.withQueryParam("journalpostliste", equalTo(JOURNALPOSTID1))
				.withQueryParam("journalpostliste", equalTo(JOURNALPOSTID2))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBody(classpathToString(responsebody))));
	}

	private void stubPutOppdaterForsendelse(String forsendelsesId) {
		stubFor(put(OPPDATERFORSENDELSE_URL)
				.withRequestBody(containing("\"forsendelseId\":" + forsendelsesId))
				.withRequestBody(containing("\"forsendelseStatus\":\"KLAR_FOR_DIST\""))
				.willReturn(aResponse()
						.withStatus(OK.value())));
	}

	private void stubPutFeilregistrerforsendelse(String forsendelsesId) {
		stubFor(put(FEILREGISTRERFORSENDELSE_URL)
				.withRequestBody(containing("\"forsendelseId\":" + forsendelsesId))
				.withRequestBody(containing("\"feilTypeCode\":\"MELDINGSFEIL\""))
				.withRequestBody(containing("\"detaljer\":\"Forsendelse til NAV.NO er ikke lest innen frist.\""))
				.willReturn(aResponse()
						.withStatus(OK.value())));
	}

	private void stubPostOpprettForsendelse(String responseBody, String oldBestillingsId) throws IOException {
		stubFor(post(urlEqualTo("/rest/v1/administrerforsendelse"))
				.withRequestBody(containing("\"originalDistribusjonId\":" + "\"" + oldBestillingsId + "\""))
				.withRequestBody(containing("\"distribusjonsKanal\":\"PRINT\""))
				.withRequestBody(containing("\"dokumenttypeId\":\"U000001\""))
				.withRequestBody(matchingJsonPath("$.bestillingsId"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withStatus(OK.value())
						.withBody(classpathToString(responseBody))));
	}

	private void stubPatchOppdaterDistribusjonsinfo() {
		stubFor(patch(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL))
				.withRequestBody(containing("\"settStatusEkspedert\":false"))
				.withRequestBody(containing("\"utsendingsKanal\":\"S\""))
				.withRequestBody(containing("\"tilbakestillJournalpost\":true"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withStatus(OK.value())));
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
		Consumer<String, DoknotifikasjonStopp> consumer = setUpConsumerForTopicNotifikasjonStopp();
		return StreamSupport.stream(getRecords(consumer, Duration.ofSeconds(10))
						.records(RENOTIFIKASJON_STOPP_TOPIC).spliterator(), false)
				.map(ConsumerRecord::value)
				.collect(Collectors.toList());
	}

	public Consumer<String, DoknotifikasjonStopp> setUpConsumerForTopicNotifikasjonStopp() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test", "true", kafkaEmbedded);
		consumerProps.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put(VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
		consumerProps.put(SCHEMA_REGISTRY_URL_CONFIG, "mock://localhost");
		consumerProps.put(SPECIFIC_AVRO_READER_CONFIG, "true");
		consumerProps.put(GROUP_INSTANCE_ID_CONFIG, "itest-group-instance");

		var consumer = new DefaultKafkaConsumerFactory<String, DoknotifikasjonStopp>(consumerProps).createConsumer();
		consumer.subscribe(singletonList(RENOTIFIKASJON_STOPP_TOPIC));
		return consumer;
	}

	public static String classpathToString(String path) throws IOException {
		InputStream inputStream = new ClassPathResource(path).getInputStream();
		String message = new String(inputStream.readAllBytes(), UTF_8);
		IOUtils.closeQuietly(inputStream);
		return message;
	}
}
