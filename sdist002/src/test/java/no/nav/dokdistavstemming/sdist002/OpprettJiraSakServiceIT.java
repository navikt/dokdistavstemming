package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.JiraFunctionalException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
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

@Disabled("Disse testene kjører OK lokalt, men feiler på GitHub. Disabler inntil videre.")
public class OpprettJiraSakServiceIT extends AbstractSdist002ITest {

	private static final String JIRA_MESSAGE = "https://jira-q1.adeo.no/browse/MMA-134";
	private static final LocalDate AVSTEMMINGSDATO = LocalDate.of(2025,1,1);

	@Autowired
	private JiraOppgaveService jiraOppgaveService;

	@Override
	protected void setupResources() {
		postAzureToken();
	}

	@Test
	void shouldHappyOppretteJiraSak() throws Exception {
		happilyHentUekspederteForsendelser("henteforsendelse-print-overfemdager.json");
		jiraHappyHentProjectDetails();
		jiraHappyOpprettSakForAvstemForsendelse();
		jiraHappyPostVedleggDokument();
		jiraHappyUpdateSak("MMA-134");
		jiraHappyGetIssue();
		List<UekspedertForsendelseDokument> result = sdist002Service.getForsendelserByDistribusjonKanal(PRINT);
		byte[] csv = csvProducer.oppretteCsv(result, PRINT);

		JiraSakResponseTo jiraSakResponseTo = jiraOppgaveService.opprettJirasak(PRINT, csv, result.size(), AVSTEMMINGSDATO);

		assertThat(jiraSakResponseTo.getMessage()).isEqualTo(JIRA_MESSAGE);
		assertThat(jiraSakResponseTo.getHttpStatusCode()).isEqualTo(201);
		assertTrue(csv.length != 0);
		verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)).withRequestBody(equalToJson(classpathToString("__files/jira/jirarequest-happy.json"))));
		verify(1, postRequestedFor(urlEqualTo(JIRA_VEDLEGG_URL)));
		verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
	}

	@Test
	void shouldHappyOppretteJiraSakForEhandel() throws Exception {
		happilyHentUekspederteForsendelser("hentuekspederteforsendelser-ehandel.json");
		jiraHappyOpprettSakForAvstemForsendelse();
		jiraHappyHentProjectDetails();
		jiraHappyPostVedleggDokument();
		jiraHappyUpdateSak("MMA-134");
		jiraHappyGetIssue();
		List<UekspedertForsendelseDokument> result = sdist002Service.getForsendelserByDistribusjonKanal(E_HANDEL);
		byte[] csv = csvProducer.oppretteCsv(result, E_HANDEL);

		JiraSakResponseTo jiraSakResponseTo = jiraOppgaveService.opprettJirasak(E_HANDEL, csv, result.size(), AVSTEMMINGSDATO);

		assertThat(jiraSakResponseTo.getMessage()).isEqualTo(JIRA_MESSAGE);
		assertThat(jiraSakResponseTo.getHttpStatusCode()).isEqualTo(201);
		assertTrue(csv.length != 0);
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
		byte[] csv = csvProducer.oppretteCsv(result, PRINT);

		var exception = assertThrows(JiraFunctionalException.class, () -> jiraOppgaveService.opprettJirasak(PRINT, csv, result.size(), AVSTEMMINGSDATO));

		assertThat(exception.getMessage()).contains("opprettJira feilet med status=400 feilmelding");
		assertTrue(csv.length != 0);
		verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)));
		verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
	}
}
