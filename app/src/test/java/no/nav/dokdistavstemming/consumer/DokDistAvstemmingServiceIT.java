package no.nav.dokdistavstemming.consumer;


import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdistavstemming.AbstractIT;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingResponseTo;
import no.nav.dokdistavstemming.mdc.MDCConstants;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.service.serviceimp.DokDistAvstemmingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISRIBUSJON_DATO_J;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL_P_J;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS_J;
import static no.nav.dokdistavstemming.utils.TestDataUtils.FORSENDELSE_ID_1_J;
import static no.nav.dokdistavstemming.utils.TestDataUtils.FORSENDELSE_ID_J;
import static no.nav.dokdistavstemming.utils.WireMockResponse.dokDistHappyHentUekspedereFrosendelse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.dokDistHappyHentUekspedereFrosendelseKanalPrint;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class DokDistAvstemmingServiceIT extends AbstractIT {

	private static String CALL_ID = UUID.randomUUID().toString();

	@Inject
	private DokDistAvstemmingService dokDistAvstemmingService;
	@Inject
	private HentUekspederForsendelse hentUekspederKvitteringForsendelse;
	@Inject
	private CSVProdusere csvProdusere;


	@BeforeEach
	public void setUp() {
		dokDistAvstemmingService = new DokDistAvstemmingService(hentUekspederKvitteringForsendelse, csvProdusere);
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
		MDC.put(MDCConstants.MDC_CALL_ID, CALL_ID);

	}

	@Test
	public void shouldHentListOkStatus() throws Exception {
		dokDistHappyHentUekspedereFrosendelse();
		List<DokDistAvstemmingResponseTo> dokDistAvstemmingForsendels = dokDistAvstemmingService.dokDistAvstemmingUtenPrintJiraSak();
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/SDP/6")));
		assertThat(dokDistAvstemmingForsendels.get(0).getDistribusjonId(), is(FORSENDELSE_ID_1_J));
	}


	@Test
	public void shouldHentListOkStatusKanalPrint() throws Exception {
		dokDistHappyHentUekspedereFrosendelseKanalPrint();
		List<DokDistAvstemmingResponseTo> result = dokDistAvstemmingService.dokDistAvstemmingUekspederrKanalPrint();
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/PRINT/120")));
		assertThat(result.get(0).getDistribusjonId(), is(FORSENDELSE_ID_J));
		assertThat(result.get(0).getDistribusjonStatus(), is(DISTRIBUSJON_STATUS_J));
		assertThat(result.get(0).getDistribusjonKanal(), is(DISTRIBUSJON_KANAL_P_J.name()));
		assertThat(result.get(0).getCountDokument(), is(10L));
		assertThat(result.get(0).getDistribusjonDato().toString(), is(DISRIBUSJON_DATO_J));

	}

	@Test
	public void shouldOppretteCSVFilList() throws Exception {

		dokDistHappyHentUekspedereFrosendelseKanalPrint();
		List<DokDistAvstemmingResponseTo> result = dokDistAvstemmingService.dokDistAvstemmingUekspederrKanalPrint();
		File csvFiler = csvProdusere.oppretteCsvFil(result);
		assertThat(csvFiler.isFile(), is(true));
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/PRINT/120")));

	}


}
