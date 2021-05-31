package no.nav.dokdistavstemming.avstemmIT;


import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.AbstractIT;
import no.nav.dokdistavstemming.config.DokdistavstemmingProp;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseTechnicalException;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import no.nav.dokdistavstemming.service.serviceimp.Sdist002Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISRIBUSJON_DATO_J;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_ID_PRINT;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_ID_SDP;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL_P_J;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS_J;
import static no.nav.dokdistavstemming.utils.TestUtils.classpathToString;
import static no.nav.dokdistavstemming.utils.WireMockResponse.ADMINISTRERFORSENDELSE_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.JIRA_MMA_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.JIRA_OPPRETTE_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.JIRA_VEDLEGG_URL;
import static no.nav.dokdistavstemming.utils.WireMockResponse.dokDistHappyHentUekspedereFrosendelse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.happilyHentForsendelseKvitteringIkkeMottattKanalPrint;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappilyUpdateSaken;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyGetIssue;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyHentProjectDetails;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyOpprettSakForAvstemFrosendelse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.jiraHappyPostVedleggDokument;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterAvstemFrosendelseInfo;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterAvstemFrosendelseInfoFeil;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterAvstemFrosendelseInfoFeilWithInternalServerError;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

public class Sdist002ServiceIT extends AbstractIT {


    @Inject
    private Sdist002Service sdist002Service;
    @Inject
    private HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt;
    @Inject
    private CSVProdusere csvProdusere;
    @Inject
    private MeterRegistry meterRegistry;
    @Inject
    private JiraService jiraService;
    @Inject
    private DokdistavstemmingProp dokdistavstemmingProp;

    @BeforeEach
    public void setUp() {
        WireMock.reset();
        WireMock.resetAllRequests();
        WireMock.removeAllMappings();
        sdist002Service = new Sdist002Service(hentForsendelseKvitteringIkkeMottatt, csvProdusere, meterRegistry, jiraService, dokdistavstemmingProp);

    }

    @Test
    public void shouldHentListOkStatus() throws Exception {
        dokDistHappyHentUekspedereFrosendelse();
        List<AvstemForsendelseResponseTo> dokDistAvstemmingForsendels = sdist002Service.getForsendelserByDistirbusjonKanal(SDP.name());
        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/SDP/10")));
        assertThat(dokDistAvstemmingForsendels.get(0).getDistribusjonId(), is(DISTRIBUSJON_ID_SDP));
    }


    @Test
    public void shouldHentListOkStatusKanalPrint() throws Exception {
        happilyHentForsendelseKvitteringIkkeMottattKanalPrint("__files/rdist001/henteforsendelse-print-overfemdager.json");
        List<AvstemForsendelseResponseTo> result = sdist002Service.getForsendelserByDistirbusjonKanal(PRINT.name());
        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/PRINT/120")));
        assertThat(result.get(0).getDistribusjonId(), is(DISTRIBUSJON_ID_PRINT));
        assertThat(result.get(0).getDistribusjonStatus(), is(DISTRIBUSJON_STATUS_J));
        assertThat(result.get(0).getDistribusjonKanal(), is(DISTRIBUSJON_KANAL_P_J.name()));
        assertThat(result.get(0).getDistribusjonDato().toString(), is(DISRIBUSJON_DATO_J));

    }

    @Test
    public void shouldOppretteCSVFilList() throws Exception {

        happilyHentForsendelseKvitteringIkkeMottattKanalPrint("__files/rdist001/henteforsendelse-print-overfemdager.json");
        List<AvstemForsendelseResponseTo> result = sdist002Service.getForsendelserByDistirbusjonKanal(PRINT.name());
        File csvFiler = csvProdusere.oppretteCsvFil(result);
        assertThat(csvFiler.isFile(), is(true));
        assertThat(csvFiler.length() != 0, is(true));
        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/PRINT/120")));

    }

    @Test
    public void shouldOppdatertForsendelserAvstemDatoOgReferanse() throws Exception {
        happilyHentForsendelseKvitteringIkkeMottattKanalPrint("__files/rdist001/henteforsendelse-print-overfemdager.json");
        jiraHappyHentProjectDetails();
        jiraHappyOpprettSakForAvstemFrosendelse();
        jiraHappyPostVedleggDokument();
        oppdaterAvstemFrosendelseInfo();
        jiraHappilyUpdateSaken("MMA-134");
        jiraHappyGetIssue();
        sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal();

        verify(7, putRequestedFor(urlEqualTo(ADMINISTRERFORSENDELSE_URL))
                .withRequestBody(equalToJson(classpathToString("__files/rdist001/oppdaterForsendelserAvstemtInfo_Ok.json"))));

    }

    @Test
    public void shouldOppdatertForsendelserThrowsBadRequestException() throws Exception {
        dokDistHappyHentUekspedereFrosendelse();
        jiraHappyHentProjectDetails();
        jiraHappyOpprettSakForAvstemFrosendelse();
        jiraHappyPostVedleggDokument();
        oppdaterAvstemFrosendelseInfoFeil();
        jiraHappilyUpdateSaken("MMA-134");
        jiraHappyGetIssue();
        assertThrows(AvstemForsendelseFunctionalException.class, () -> sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal());

        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/SDP/10")));
        verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)));
        verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
        verify(1, postRequestedFor(urlEqualTo(JIRA_VEDLEGG_URL)));
        verify(1, putRequestedFor(urlEqualTo(ADMINISTRERFORSENDELSE_URL)));

    }

    @Test
    public void shouldOppdatertForsendelserThrowsInternalServerException() throws Exception {
        dokDistHappyHentUekspedereFrosendelse();
        jiraHappyHentProjectDetails();
        jiraHappyOpprettSakForAvstemFrosendelse();
        jiraHappyPostVedleggDokument();
        oppdaterAvstemFrosendelseInfoFeilWithInternalServerError();
        jiraHappilyUpdateSaken("MMA-134");
        jiraHappyGetIssue();
        assertThrows(AvstemForsendelseTechnicalException.class, () -> sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal());
        verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/SDP/10")));
        verify(1, postRequestedFor(urlEqualTo(JIRA_OPPRETTE_URL)));
        verify(1, getRequestedFor(urlEqualTo(JIRA_MMA_URL)));
        verify(1, postRequestedFor(urlEqualTo(JIRA_VEDLEGG_URL)));
        verify(3, putRequestedFor(urlEqualTo(ADMINISTRERFORSENDELSE_URL)));

    }


}
