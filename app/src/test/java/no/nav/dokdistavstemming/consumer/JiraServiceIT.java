package no.nav.dokdistavstemming.consumer;


import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdistavstemming.AbstractIT;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.mdc.MDCConstants;
import no.nav.dokdistavstemming.service.serviceimp.DokDistAvstemmingService;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import no.nav.dokdistavstemming.utils.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.slf4j.MDC;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createIssueResponse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.dokDistHappyHentUekspedereFrosendelse;
import static no.nav.dokdistavstemming.utils.WireMockResponse.dokDistHappyOppretteJiraSak;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest,wiremock")
public class JiraServiceIT extends AbstractIT {

	private static String CALL_ID = UUID.randomUUID().toString();

	@Inject
	private DokDistAvstemmingService dokDistAvstemmingService;
	@Inject
	private JiraConsumer jiraConsumer;

	@InjectMocks
	private JiraService jiraService;


	@BeforeEach
	public void setUp() {
		jiraService = new JiraService(jiraConsumer, dokDistAvstemmingService);
		when(jiraConsumer.oppretteJiraSak(TestDataUtils.createJiraSaksRequest())).thenReturn(createIssueResponse());
		when(dokDistAvstemmingService.dokDistAvstemmingUtenPrintJiraSak()).thenReturn(TestDataUtils.createDokDistAvstemmingSDP());
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
		MDC.put(MDCConstants.MDC_CALL_ID, CALL_ID);

	}

	@Test
	public void shouldCreateJiraSaksOkStatus() throws Exception {
		dokDistHappyOppretteJiraSak();
		dokDistHappyHentUekspedereFrosendelse();
		List<File> csvfil = dokDistAvstemmingService.henteDokDistFil();
		jiraService.createJiraSak();
		verify(1, postRequestedFor(urlEqualTo("/rest/api/2/issue")));

	}


}
