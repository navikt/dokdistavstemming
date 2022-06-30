package no.nav.dokdistavstemming.avstemmIT;

import no.nav.dokdistavstemming.AbstractIT;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.E_HANDEL;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.utils.TestUtils.classpathToString;
import static no.nav.dokdistavstemming.utils.WireMockResponse.JIRA_MMA_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.JIRA_OPPRETTE_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.JIRA_VEDLEGG_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.happilyHentForsendelseKvitteringIkkeMottattKanalPrint;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraFeilToOpprettSakForAvstemFrosendelse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyGetIssue;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyHentProjectDetails;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyOpprettSakForAvstemFrosendelse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyPostVedleggDokument;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyUpdateSak;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */


public class OpprettJiraSakServiceIT extends AbstractIT {

    private static final String JIRA_MESSAGE = "https://jira-q1.adeo.no/browse/MMA-134";

    @BeforeEach
    public void setUp() {
        super.setUp();
        jiraService = new JiraService(jiraConsumer);
    }

    @Test
    void shouldHappilyOppretteJiraSak() throws Exception {
        happilyHentForsendelseKvitteringIkkeMottattKanalPrint("__files/rdist001/henteforsendelse-print-overfemdager.json");
        jiraHappyHentProjectDetails();
        jiraHappyOpprettSakForAvstemFrosendelse();
        jiraHappyPostVedleggDokument();
        jiraHappyUpdateSak("MMA-134");
        jiraHappyGetIssue();
        List<AvstemForsendelseResponseTo> result = sdist002Service.getForsendelserByDistribusjonKanal(PRINT.name());
        File fil = csvProdusere.oppretteCsvFil(result);
        JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak(PRINT.name(), fil, result.size());

        assertThat(jiraSakResponseTo.getMessage(), is(JIRA_MESSAGE));
        assertThat(jiraSakResponseTo.getHttpStatusCode(), is(0));
        assertTrue(fil.exists());
        assertTrue(fil.length() != 0);
        verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)).withRequestBody(equalToJson(classpathToString("__files/jira/jirarequest-happy.json"))));
        verify(1, postRequestedFor(urlEqualTo(JIRA_VEDLEGG_URL)));
        verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
    }

    @Test
    void shouldHappOppretteJiraSakForEhandel() throws Exception {
        happilyHentForsendelseKvitteringIkkeMottattKanalPrint("__files/rdist001/ehandel.json");
        jiraHappyHentProjectDetails();
        jiraHappyOpprettSakForAvstemFrosendelse();
        jiraHappyPostVedleggDokument();
        jiraHappyUpdateSak("MMA-134");
        jiraHappyGetIssue();
        List<AvstemForsendelseResponseTo> result = sdist002Service.getForsendelserByDistribusjonKanal(E_HANDEL.name());
        File fil = csvProdusere.oppretteCsvFil(result);
        JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak(E_HANDEL.name(), fil, result.size());

        assertThat(jiraSakResponseTo.getMessage(), is(JIRA_MESSAGE));
        assertThat(jiraSakResponseTo.getHttpStatusCode(), is(0));
        assertTrue(fil.exists());
        assertTrue(fil.length() != 0);
        verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)).withRequestBody(equalToJson(classpathToString("__files/jira/ehandel-request.json"))));
        verify(1, postRequestedFor(urlEqualTo(JIRA_VEDLEGG_URL)));
        verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
    }


    @Test
    void opprettJiraSakThrowsBadRequestErrorMelding() throws Exception {
        happilyHentForsendelseKvitteringIkkeMottattKanalPrint("__files/rdist001/henteforsendelse-print-overfemdager.json");
        List<AvstemForsendelseResponseTo> result = sdist002Service.getForsendelserByDistribusjonKanal(PRINT.name());
        jiraHappyHentProjectDetails();
        jiraFeilToOpprettSakForAvstemFrosendelse();
        jiraHappyPostVedleggDokument();
        File fil = csvProdusere.oppretteCsvFil(result);

        AvstemForsendelseFunctionalException avstemForsendelseFunctionalException = assertThrows(AvstemForsendelseFunctionalException.class, () ->
                jiraService.oppretteMMAJiraSak(PRINT.name(), fil, result.size()));

        assertThat(avstemForsendelseFunctionalException.getMessage(), containsString("status:400 BAD_REQUEST ,feilmelding: 400 Bad Request"));
        assertTrue(fil.exists());
        assertTrue(fil.length() != 0);
        verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)));
        verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
    }
}
