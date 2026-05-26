package no.nav.dokdistavstemming.sdist004;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdistavstemming.config.AzureProperties;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.DokdistavstemmingServiceuser;
import no.nav.dokdistavstemming.config.JiraAuthProperties;
import no.nav.dokdistavstemming.config.SlackProperties;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;
import org.springframework.context.annotation.Configuration;
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
@SpringBootTest(
		classes = {
				AbstractSdist004ITest.TestConfig.class
		},
		webEnvironment = RANDOM_PORT
)
@EnableWireMock
public abstract class AbstractSdist004ITest {
	public static String CALL_ID = UUID.randomUUID().toString();

	@BeforeEach
	public void setUp() {
		WireMock.resetAllRequests();
		WireMock.reset();
		MDC.put(MDC_CALL_ID, CALL_ID);
	}

	@Configuration
	@EnableAutoConfiguration
	@ComponentScan(basePackages = "no.nav.dokdistavstemming")
	static class TestConfig {
	}
}
