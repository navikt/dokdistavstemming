package no.nav.dokdistavstemming.consumer;


import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdistavstemming.AbstractIT;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;
import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;
import no.nav.dokdistavstemming.mdc.MDCConstants;
import no.nav.dokdistavstemming.scheduler.LeaderElection;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.service.DokDistAvstemmingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.utils.TestDataUtil.DISRIBUSJON_DATO;
import static no.nav.dokdistavstemming.utils.TestDataUtil.DISTRIBUSJON_KANAL_P;
import static no.nav.dokdistavstemming.utils.TestDataUtil.DISTRIBUSJON_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtil.FORSENDELSE_ID;
import static no.nav.dokdistavstemming.utils.TestDataUtil.FORSENDELSE_ID_1;
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
	@Inject
	private LeaderElection leaderElection;


	@BeforeEach
	public void setUp() {
		dokDistAvstemmingService = new DokDistAvstemmingService(hentUekspederKvitteringForsendelse,csvProdusere,leaderElection);
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
		MDC.put(MDCConstants.MDC_CALL_ID, CALL_ID);
	}

	@Test
	public void shouldHentListOkStatus() throws Exception {
		dokDistHappyHentUekspedereFrosendelse();
		List<DokDistAvStemmingResponseTo> dokDistAvstemmingForsendels = dokDistAvstemmingService.dokDistAvstemmingUtenPrintJiraSak();
		verify(1, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/SDP/6")));
		assertThat(dokDistAvstemmingForsendels.get(0).getForsendelseId(), is(FORSENDELSE_ID_1));
	}


	@Test
	public void shouldHentListOkStatusKanalPrint() throws Exception {
		dokDistHappyHentUekspedereFrosendelseKanalPrint();
		List<DokDistAvStemmingResponseTo> dokDistAvstemmingForsendels = dokDistAvstemmingService.dokDistAvstemmingPrintJiraSak();
		List<DokDistAvStemmingResponseTo> result = dokDistAvstemmingService.dokDistAvstemmingPrintJiraSak();
		verify(2, getRequestedFor(urlEqualTo("/administrerforsendelse/henteuekspederforsendelse/PRINT/120")));
		assertThat(result.get(0).getForsendelseId(),is(FORSENDELSE_ID));
		assertThat(result.get(0).getDistribusjonStatus(),is(DISTRIBUSJON_STATUS));
		assertThat(result.get(0).getDistribusjonKanal().name(),is(DISTRIBUSJON_KANAL_P));
		assertThat(result.get(0).getCountDokument(),is(10L));
		assertThat(result.get(0).getDistribusjonDato().toString(),is(DISRIBUSJON_DATO));

	}



}
