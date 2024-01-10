package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.JiraFunctionalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.E_HANDEL;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.sdist002.TestUtils.classpathToString;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.JIRA_MMA_URL;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.JIRA_OPPRETTE_URL;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.JIRA_VEDLEGG_URL;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.happilyHentUekspederteForsendelser;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraFeilToOpprettSakForAvstemForsendelse;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyGetIssue;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyHentProjectDetails;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyOpprettSakForAvstemForsendelse;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyPostVedleggDokument;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyUpdateSak;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.postAzureToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpprettJiraSakServiceIT extends AbstractSdist002ITest {

	private static final String JIRA_MESSAGE = "https://jira-q1.adeo.no/browse/MMA-134";

	@Autowired
	private JiraService jiraService;

	@BeforeEach
	public void setUp() {
		postAzureToken();
	}

	@Test
	void shouldHappilyOppretteJiraSak() throws Exception {
		happilyHentUekspederteForsendelser("henteforsendelse-print-overfemdager.json");
		jiraHappyHentProjectDetails();
		jiraHappyOpprettSakForAvstemForsendelse();
		jiraHappyPostVedleggDokument();
		jiraHappyUpdateSak("MMA-134");
		jiraHappyGetIssue();
		List<UekspedertForsendelseDokument> result = sdist002Service.getForsendelserByDistribusjonKanal(PRINT);
		File fil = csvProdusere.oppretteCsvFil(result);

		JiraSakResponseTo jiraSakResponseTo = jiraService.opprettJirasak(PRINT.name(), fil, result.size());

		assertThat(jiraSakResponseTo.getMessage()).isEqualTo(JIRA_MESSAGE);
		assertThat(jiraSakResponseTo.getHttpStatusCode()).isEqualTo(0);
		assertTrue(fil.exists());
		assertTrue(fil.length() != 0);
		verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)).withRequestBody(equalToJson(classpathToString("__files/jira/jirarequest-happy.json"))));
		verify(1, postRequestedFor(urlEqualTo(JIRA_VEDLEGG_URL)));
		verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
	}

	@Test
	void shouldHappOppretteJiraSakForEhandel() throws Exception {
		happilyHentUekspederteForsendelser("hentuekspederteforsendelser-ehandel.json");
		jiraHappyHentProjectDetails();
		jiraHappyOpprettSakForAvstemForsendelse();
		jiraHappyPostVedleggDokument();
		jiraHappyUpdateSak("MMA-134");
		jiraHappyGetIssue();
		List<UekspedertForsendelseDokument> result = sdist002Service.getForsendelserByDistribusjonKanal(E_HANDEL);
		File fil = csvProdusere.oppretteCsvFil(result);

		JiraSakResponseTo jiraSakResponseTo = jiraService.opprettJirasak(E_HANDEL.name(), fil, result.size());

		assertThat(jiraSakResponseTo.getMessage()).isEqualTo(JIRA_MESSAGE);
		assertThat(jiraSakResponseTo.getHttpStatusCode()).isEqualTo(0);
		assertTrue(fil.exists());
		assertTrue(fil.length() != 0);
		verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)).withRequestBody(equalToJson(classpathToString("__files/jira/ehandel-request.json"))));
		verify(1, postRequestedFor(urlEqualTo(JIRA_VEDLEGG_URL)));
		verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
	}

	@Test
	void opprettJiraSakThrowsBadRequestErrorMelding() {
		happilyHentUekspederteForsendelser("henteforsendelse-print-overfemdager.json");
		List<UekspedertForsendelseDokument> result = sdist002Service.getForsendelserByDistribusjonKanal(PRINT);
		jiraHappyHentProjectDetails();
		jiraFeilToOpprettSakForAvstemForsendelse();
		jiraHappyPostVedleggDokument();
		File fil = csvProdusere.oppretteCsvFil(result);

		var exception = assertThrows(JiraFunctionalException.class, () -> jiraService.opprettJirasak(PRINT.name(), fil, result.size()));

		assertThat(exception.getMessage()).contains("status=400 BAD_REQUEST, feilmelding=400 Bad Request");
		assertTrue(fil.exists());
		assertTrue(fil.length() != 0);
		verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)));
		verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
	}
}
