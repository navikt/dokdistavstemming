package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.exceptions.DokdistavstemmingFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingTechnicalException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.WireMockResponse.AVSTEM_EKSPEDERTE_FORSENDELSER_URL;
import static no.nav.dokdistavstemming.WireMockResponse.HENT_EKSPEDERTE_FORSENDELSER_URL;
import static no.nav.dokdistavstemming.WireMockResponse.JOURNALPOST_API_URL;
import static no.nav.dokdistavstemming.WireMockResponse.getEkspederteForsendelser;
import static no.nav.dokdistavstemming.WireMockResponse.oppdaterAvstemArkivForsendelseInfo;
import static no.nav.dokdistavstemming.WireMockResponse.oppdaterJournalpost;
import static no.nav.dokdistavstemming.WireMockResponse.oppdaterJournalpostFeil;
import static no.nav.dokdistavstemming.WireMockResponse.postAzureToken;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BulkOppdaterJournalpostDistInfoServiceIT extends AbstractSdist004ITest {

	@Autowired
	private BulkOppdaterJournalpostDistInfoService sdist004BulkOppdaterService;

	@Test
	public void shouldHenteBulkForsendelseOgOppdatereJouralpost() {
		getEkspederteForsendelser("ekspedertforsendelse.json");
		oppdaterAvstemArkivForsendelseInfo();
		oppdaterJournalpost("journalpost_distinfo_ok_response.json");
		postAzureToken();

		sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo();

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(1, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}

	@Test
	public void shouldNotUpdateAvstemDatoIDokdistWhenJouralpostResponseIsNullOrEmpty() {
		getEkspederteForsendelser("ekspedertforsendelse.json");
		oppdaterAvstemArkivForsendelseInfo();
		oppdaterJournalpost("journalpost_distinfo_empty_response.json");
		postAzureToken();

		sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo();

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}

	@Test
	public void shouldHenteBulkForsendelseOgFeilTilOppdatereJournalpost() {
		getEkspederteForsendelser("ekspedertforsendelse-jp-feil-response.json");
		oppdaterAvstemArkivForsendelseInfo();
		oppdaterJournalpost("journalpost_distinfo_feil_response.json");
		postAzureToken();

		sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo();

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}

	@Test
	public void shouldJournalpostApiThrowBadRequestException() {
		getEkspederteForsendelser("ekspedertforsendelse.json");
		oppdaterJournalpostFeil(BAD_REQUEST);
		postAzureToken();

		assertThrows(DokdistavstemmingFunctionalException.class, () -> sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo());

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(1, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}

	@Test
	public void shouldJournalpostApiThrowTechnicalException() {
		getEkspederteForsendelser("ekspedertforsendelse.json");
		oppdaterJournalpostFeil(HttpStatus.INTERNAL_SERVER_ERROR);
		postAzureToken();

		assertThrows(DokdistavstemmingTechnicalException.class, () -> sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo());

		verify(1, getRequestedFor(urlEqualTo(HENT_EKSPEDERTE_FORSENDELSER_URL)));
		verify(3, postRequestedFor(urlEqualTo(JOURNALPOST_API_URL)));
		verify(0, putRequestedFor(urlEqualTo(AVSTEM_EKSPEDERTE_FORSENDELSER_URL)));
	}
}
