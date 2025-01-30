package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingTechnicalException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.E_HANDEL;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISRIBUSJON_DATO_J;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_ID_PRINT;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_ID_SDP;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_KANAL_P_J;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_STATUS_J;
import static no.nav.dokdistavstemming.sdist002.TestUtils.classpathToString;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.AVSTEM_FORSENDELSER_URL;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.JIRA_MMA_URL;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.JIRA_OPPRETTE_URL;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.JIRA_VEDLEGG_URL;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.STATUS_TRANSITION;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.happilyHentUekspederteForsendelser;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyGetIssue;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyHentProjectDetails;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyOpprettSakForAvstemForsendelse;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyPostVedleggDokument;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.jiraHappyUpdateSak;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.oppdaterAvstemForsendelseInfo;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.oppdaterAvstemForsendelseInfoFeilWithInternalServerError;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.oppdaterAvstemForsendelsesinfoFeil;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.postAzureToken;
import static no.nav.dokdistavstemming.sdist002.WireMockResponse.returnNoContentForHentUekspederteForsendelser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sdist002ServiceIT extends AbstractSdist002ITest {

	@Autowired
	private Sdist002Service sdist002Service;
	@Autowired
	private CSVProdusere csvProdusere;

	@Override
	protected void setupResources() {
		postAzureToken();
	}

	@Test
	public void shouldReturnUekspederteForsendelser() {
		happilyHentUekspederteForsendelser("hentForsendelse-SDP-SixTime.json");

		List<UekspedertForsendelseDokument> uekspedertForsendelseDokumentList = sdist002Service.getForsendelserByDistribusjonKanal(SDP);

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/hentuekspederteforsendelser/SDP/10")));
		assertThat(uekspedertForsendelseDokumentList.getFirst().getDistribusjonId()).isEqualTo(DISTRIBUSJON_ID_SDP);
	}

	@Test
	void shouldReturnEmptyListOnNoContentFromHentUekspederteForsendelser() {
		returnNoContentForHentUekspederteForsendelser();

		List<UekspedertForsendelseDokument> result = sdist002Service.getForsendelserByDistribusjonKanal(E_HANDEL);

		assertTrue(result.isEmpty());
	}

	@Test
	public void shouldHentListOkStatusKanalPrint() {
		happilyHentUekspederteForsendelser("henteforsendelse-print-overfemdager.json");

		List<UekspedertForsendelseDokument> result = sdist002Service.getForsendelserByDistribusjonKanal(PRINT);

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/hentuekspederteforsendelser/PRINT/120")));
		assertThat(result.getFirst().getDistribusjonId()).isEqualTo(DISTRIBUSJON_ID_PRINT);
		assertThat(result.getFirst().getDistribusjonStatus()).isEqualTo(DISTRIBUSJON_STATUS_J);
		assertThat(result.getFirst().getDistribusjonKanal()).isEqualTo(DISTRIBUSJON_KANAL_P_J.name());
		assertThat(result.getFirst().getDistribusjonDato()).isEqualTo(DISRIBUSJON_DATO_J);
	}

	@Test
	public void shouldOppretteCSVFilList() {
		happilyHentUekspederteForsendelser("henteforsendelse-print-overfemdager.json");

		List<UekspedertForsendelseDokument> result = sdist002Service.getForsendelserByDistribusjonKanal(PRINT);

		File csvFiler = csvProdusere.oppretteCsvFil(result);
		assertThat(csvFiler.isFile()).isTrue();
		assertThat(csvFiler.length() != 0).isTrue();
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/hentuekspederteforsendelser/PRINT/120")));
	}

	@Test
	public void shouldOppdatertForsendelserAvstemDatoOgReferanse() throws Exception {
		happilyHentUekspederteForsendelser("henteforsendelse-print-overfemdager.json");
		jiraHappyHentProjectDetails();
		jiraHappyOpprettSakForAvstemForsendelse();
		jiraHappyPostVedleggDokument();
		oppdaterAvstemForsendelseInfo();
		jiraHappyUpdateSak("MMA-134");
		jiraHappyGetIssue();

		sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal();

		verify(6, putRequestedFor(urlEqualTo(AVSTEM_FORSENDELSER_URL))
				.withRequestBody(equalToJson(classpathToString("__files/rdist001/oppdaterForsendelserAvstemtInfo_Ok.json"))));
	}

	@Test
	public void shouldOppdatertForsendelserThrowsBadRequestException() {

		happilyHentUekspederteForsendelser("hentForsendelse-SDP-SixTime.json");
		jiraHappyHentProjectDetails();
		jiraHappyOpprettSakForAvstemForsendelse();
		jiraHappyPostVedleggDokument();
		jiraHappyUpdateSak("MMA-134");
		jiraHappyGetIssue();
		oppdaterAvstemForsendelsesinfoFeil();

		assertThrows(DokdistavstemmingFunctionalException.class, () -> sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal());

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/hentuekspederteforsendelser/SDP/10")));
	}

	@Test
	public void shouldOppdatertForsendelserThrowsInternalServerException() {
		happilyHentUekspederteForsendelser("hentForsendelse-SDP-SixTime.json");
		jiraHappyHentProjectDetails();
		jiraHappyOpprettSakForAvstemForsendelse();
		jiraHappyPostVedleggDokument();
		oppdaterAvstemForsendelseInfoFeilWithInternalServerError();
		jiraHappyUpdateSak("MMA-134");
		jiraHappyGetIssue();

		assertThrows(DokdistavstemmingTechnicalException.class, () -> sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal());

		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/hentuekspederteforsendelser/SDP/10")));
		verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)));
		verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
		verify(1, postRequestedFor(urlEqualTo(JIRA_VEDLEGG_URL)));
		verify(1, postRequestedFor(urlEqualTo(STATUS_TRANSITION)));
		verify(3, putRequestedFor(urlEqualTo(AVSTEM_FORSENDELSER_URL)));
	}
}
