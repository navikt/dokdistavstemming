package no.nav.dokdistavstemming;


import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.alias.JiraServiceuserAlias;
import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.mdc.MDCConstants;
import no.nav.dokdistavstemming.nais.ApplicationConfig;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.service.serviceimp.AvstemForsendelseService;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import no.nav.dokdistavstemming.utils.RequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@Profile("itest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {ApplicationConfig.class})
@EnableConfigurationProperties({ServiceuserAlias.class})
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
@ExtendWith(SpringExtension.class)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public abstract class AbstractIT {

	@Inject
	protected TestRestTemplate testRestTemplate;
	@Inject
	protected JiraServiceuserAlias jiraServiceuserAlias;

	@Inject
	public MeterRegistry meterRegistry;

	@Inject
	public JiraConsumer jiraConsumer;
	@Inject
	public JiraService jiraService;
	@Inject
	public CSVProdusere csvProdusere;
	@Inject
	public AvstemForsendelseService avstemForsendelseService;


	@Mock
	private Counter counterMock;
	public static String CALL_ID = UUID.randomUUID().toString();

	@BeforeEach
	public void setUp() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
		MDC.put(MDCConstants.MDC_CALL_ID, CALL_ID);

	}

}
