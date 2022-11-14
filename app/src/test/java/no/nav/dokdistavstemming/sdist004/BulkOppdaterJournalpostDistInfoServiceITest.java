package no.nav.dokdistavstemming.sdist004;

import no.nav.dokdistavstemming.AbstractIT;
import no.nav.dokdistavstemming.Sdist004BulkOppdaterJournalpostDistInfoService;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.Rdist001administrerforsendelse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterJournalpostDistInfoConsumer;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseTechnicalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.utils.WireMockResponse.AVSTEMFORSENDELSE_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.EKSPEDERTEFORSENDELSER_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.JOURNALPOST_API_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.getEkspederteForsendelser;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterAvstemArkivFrosendelseInfo;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterJournalpost;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterJournalpostFeil;
import static no.nav.dokdistavstemming.utils.WireMockResponse.postAzureToken;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BulkOppdaterJournalpostDistInfoServiceITest extends AbstractIT {

	@Autowired
	private Sdist004BulkOppdaterJournalpostDistInfoService sdist004BulkOppdaterService;

	@Autowired
	private Rdist001administrerforsendelse administrerforsendelse;

	@Autowired
	private BulkOppdaterJournalpostDistInfoConsumer bulkOppdaterJournalpostDistInfo;

	@BeforeEach
	public void setUp() {
		sdist004BulkOppdaterService = new Sdist004BulkOppdaterJournalpostDistInfoService(bulkOppdaterJournalpostDistInfo, administrerforsendelse);
	}

	@Test
	public void shouldHenteBulkForsendelseOgOppdatereJouralpost() throws Exception {
		getEkspederteForsendelser();
		oppdaterAvstemArkivFrosendelseInfo();
		oppdaterJournalpost("__files/journalpost/journalpost_distinfo_ok_response.json");
		postAzureToken();

		sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo();

		verify(1, getRequestedFor(urlEqualTo(EKSPEDERTEFORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(1, putRequestedFor(urlEqualTo(AVSTEMFORSENDELSE_URL)));
	}

	@Test
	public void shouldHenteBulkForsendelseOgFeilTilOppdatereJournalpost() throws Exception {
		getEkspederteForsendelser();
		oppdaterAvstemArkivFrosendelseInfo();
		oppdaterJournalpost("__files/journalpost/journalpost_distinfo_feil_response.json");
		postAzureToken();

		sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo();

		verify(1, getRequestedFor(urlEqualTo(EKSPEDERTEFORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEMFORSENDELSE_URL)));
	}

	@Test
	public void shouldJournalpostApiThrowBadRequestException() throws Exception {
		getEkspederteForsendelser();
		oppdaterJournalpostFeil(BAD_REQUEST);
		postAzureToken();

		assertThrows(AvstemForsendelseFunctionalException.class, () -> sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo());

		verify(1, getRequestedFor(urlEqualTo(EKSPEDERTEFORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEMFORSENDELSE_URL)));
	}

	@Test
	public void shouldJournalpostApiThrowTechnicalException() throws Exception {
		getEkspederteForsendelser();
		oppdaterJournalpostFeil(HttpStatus.INTERNAL_SERVER_ERROR);
		postAzureToken();

		assertThrows(AvstemForsendelseTechnicalException.class, () -> sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo());

		verify(1, getRequestedFor(urlEqualTo(EKSPEDERTEFORSENDELSER_URL)));
		verify(3, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEMFORSENDELSE_URL)));
	}
}
