package no.nav.dokdistavstemming.sdist004;

import no.nav.dokdistavstemming.AbstractIT;
import no.nav.dokdistavstemming.BulkOppdaterJournalpostDistInfoService;
import no.nav.dokdistavstemming.consumer.dokdistadmin.Rdist001administrerforsendelse;
import no.nav.dokdistavstemming.consumer.journalpostapi.DokarkivConsumer;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingTechnicalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.utils.WireMockResponse.AVSTEM_EKSPEDERTE_FORSENDELSER_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.HENT_EKSPEDERTE_FORSENDELSER_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.JOURNALPOST_API_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.getEkspederteForsendelser;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterAvstemArkivForsendelseInfo;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterJournalpost;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterJournalpostFeil;
import static no.nav.dokdistavstemming.utils.WireMockResponse.postAzureToken;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BulkOppdaterJournalpostDistInfoServiceIT extends AbstractIT {

	@Autowired
	private BulkOppdaterJournalpostDistInfoService sdist004BulkOppdaterService;

	@Autowired
	private Rdist001administrerforsendelse administrerforsendelse;

	@Autowired
	private DokarkivConsumer dokarkivConsumer;

	@BeforeEach
	public void setUp() {
		sdist004BulkOppdaterService = new BulkOppdaterJournalpostDistInfoService(dokarkivConsumer, administrerforsendelse);
	}

	@Test
	public void shouldHenteBulkForsendelseOgOppdatereJouralpost() throws Exception {
		getEkspederteForsendelser("__files/rdist001/ekspedertforsendelse.json");
		oppdaterAvstemArkivForsendelseInfo();
		oppdaterJournalpost("__files/journalpost/journalpost_distinfo_ok_response.json");
		postAzureToken();

		sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo();

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(1, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}


	@Test
	public void shouldNotUpdateAvstemDatoIDokdistWhenJouralpostResponseIsNullOrEmpty() throws Exception {
		getEkspederteForsendelser("__files/rdist001/ekspedertforsendelse.json");
		oppdaterAvstemArkivForsendelseInfo();
		oppdaterJournalpost("__files/journalpost/journalpost_distinfo_empty_response.json");
		postAzureToken();

		sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo();

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}

	@Test
	public void shouldHenteBulkForsendelseOgFeilTilOppdatereJournalpost() throws Exception {
		getEkspederteForsendelser("__files/rdist001/ekspedertforsendelse-jp-feil-response.json");
		oppdaterAvstemArkivForsendelseInfo();
		oppdaterJournalpost("__files/journalpost/journalpost_distinfo_feil_response.json");
		postAzureToken();

		sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo();

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}

	@Test
	public void shouldJournalpostApiThrowBadRequestException() throws Exception {
		getEkspederteForsendelser("__files/rdist001/ekspedertforsendelse.json");
		oppdaterJournalpostFeil(BAD_REQUEST);
		postAzureToken();

		assertThrows(DokdistavstemmingFunctionalException.class, () -> sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo());

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}

	@Test
	public void shouldJournalpostApiThrowTechnicalException() throws Exception {
		getEkspederteForsendelser("__files/rdist001/ekspedertforsendelse.json");
		oppdaterJournalpostFeil(HttpStatus.INTERNAL_SERVER_ERROR);
		postAzureToken();

		assertThrows(DokdistavstemmingTechnicalException.class, () -> sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo());

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(3, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}
}
