package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.DokdistavstemmingServiceuser;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
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
		DokdistavstemmingServiceuser.class
})
@EnableAutoConfiguration
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
@SpringBootTest(
		classes = {
				AbstractSdist004ITest.class
		},
		webEnvironment = RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
public abstract class AbstractSdist004ITest {
	public static String CALL_ID = UUID.randomUUID().toString();

	@BeforeEach
	public void setUp() {
		MDC.put(MDC_CALL_ID, CALL_ID);
	}
}
