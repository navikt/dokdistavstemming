package no.nav.dokdistavstemming.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("itest")
@EnableRetry
@EnableConfigurationProperties({
		DokdistavstemmingProperties.class,
		MqGatewayAlias.class,
		DokdistavstemmingServiceuser.class,
		AzureProperties.class
})
@Import({
		JmsItestConfig.class
})
@EnableAutoConfiguration
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
@SpringBootTest(
		classes = {
				ApplicationTestConfig.class
		},
		webEnvironment = RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
public class ApplicationTestConfig {
}
