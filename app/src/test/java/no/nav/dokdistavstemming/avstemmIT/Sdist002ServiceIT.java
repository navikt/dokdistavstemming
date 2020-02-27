package no.nav.dokdistavstemming.avstemmIT;


import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.AbstractIT;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.service.serviceimp.Sdist002Service;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISRIBUSJON_DATO_J;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL_P_J;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS_J;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_ID_SDP;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_ID_PRINT;
import static no.nav.dokdistavstemming.utils.WireMockResponse.dokDistHappyHentUekspedereFrosendelse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.happilyHentForsendelseKvitteringIkkeMottattKanalPrint;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


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

	@BeforeEach
	public void setUp() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
		sdist002Service = new Sdist002Service(hentForsendelseKvitteringIkkeMottatt, csvProdusere, meterRegistry, jiraService);

	}

	@Test
	public void shouldHentListOkStatus() throws Exception {
		dokDistHappyHentUekspedereFrosendelse();
		List<AvstemForsendelseResponseTo> dokDistAvstemmingForsendels = sdist002Service.getAvstemmForsendelseByDistKanal(SDP.name());
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/SDP/6")));
		assertThat(dokDistAvstemmingForsendels.get(0).getDistribusjonId(), is(DISTRIBUSJON_ID_SDP));
	}


	@Test
	public void shouldHentListOkStatusKanalPrint() throws Exception {
		happilyHentForsendelseKvitteringIkkeMottattKanalPrint();
		List<AvstemForsendelseResponseTo> result = sdist002Service.getAvstemmForsendelseByDistKanal(PRINT.name());
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/PRINT/120")));
		assertThat(result.get(0).getDistribusjonId(), is(DISTRIBUSJON_ID_PRINT));
		assertThat(result.get(0).getDistribusjonStatus(), is(DISTRIBUSJON_STATUS_J));
		assertThat(result.get(0).getDistribusjonKanal(), is(DISTRIBUSJON_KANAL_P_J.name()));
		assertThat(result.get(0).getDistribusjonDato().toString(), is(DISRIBUSJON_DATO_J));

	}

	@Test
	public void shouldOppretteCSVFilList() throws Exception {

		happilyHentForsendelseKvitteringIkkeMottattKanalPrint();
		List<AvstemForsendelseResponseTo> result = sdist002Service.getAvstemmForsendelseByDistKanal(PRINT.name());
		File csvFiler = csvProdusere.oppretteCsvFil(result);
		assertThat(csvFiler.isFile(), is(true));
		assertThat(csvFiler.length() != 0, is(true));
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/PRINT/120")));

	}


}
