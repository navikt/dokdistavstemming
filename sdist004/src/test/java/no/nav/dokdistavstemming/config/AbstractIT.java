package no.nav.dokdistavstemming.config;

import no.nav.dokdistavstemming.CoreConfig;
import no.nav.dokdistavstemming.constants.MDCConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@SpringBootTest(webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class})
@AutoConfigureWireMock(port = 0)
@Import({ApplicationTestConfig.class})
@ActiveProfiles("itest")
@ExtendWith(SpringExtension.class)
public abstract class AbstractIT {

	public static String CALL_ID = UUID.randomUUID().toString();

	@BeforeEach
	public void setUp() {
		MDC.put(MDCConstants.MDC_CALL_ID, CALL_ID);
	}

}
