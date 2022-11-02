package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.config.alias.ServiceuserProperties;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.constants.MDCConstants;
import no.nav.dokdistavstemming.sdist002.CSVProdusere;
import no.nav.dokdistavstemming.sdist002.serviceimp.JiraService;
import no.nav.dokdistavstemming.sdist002.serviceimp.Sdist002Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;


@Profile("itest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {ApplicationConfig.class})
@EnableConfigurationProperties({ServiceuserProperties.class})
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
@ExtendWith(SpringExtension.class)
public abstract class AbstractIT {

	@Autowired
	public JiraConsumer jiraConsumer;
	@Autowired
	public JiraService jiraService;
	@Autowired
	public CSVProdusere csvProdusere;
	@Autowired
	public Sdist002Service sdist002Service;

	public static String CALL_ID = UUID.randomUUID().toString();

	@BeforeEach
	public void setUp() {
		MDC.put(MDCConstants.MDC_CALL_ID, CALL_ID);

	}

}
