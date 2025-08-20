package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.config.AzureProperties;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.DokdistavstemmingServiceuser;
import no.nav.dokdistavstemming.config.JiraAuthProperties;
import no.nav.dokdistavstemming.config.SlackProperties;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_CALL_ID;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles("itest")
@EnableConfigurationProperties({
		DokdistavstemmingProperties.class,
		DokdistavstemmingServiceuser.class,
		JiraAuthProperties.class,
		SlackProperties.class,
		AzureProperties.class
})
@EnableAutoConfiguration
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
@SpringBootTest(
		classes = {
				AbstractSdist002ITest.class
		},
		webEnvironment = RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
public abstract class AbstractSdist002ITest {

	public static String CALL_ID = UUID.randomUUID().toString();
	@Autowired
	public CSVProducer csvProducer;
	@Autowired
	public Sdist002Service sdist002Service;

	@BeforeEach
	public void beforeEach() {
		MDC.put(MDC_CALL_ID, CALL_ID);
		setupResources();
	}

	protected abstract void setupResources();
}
