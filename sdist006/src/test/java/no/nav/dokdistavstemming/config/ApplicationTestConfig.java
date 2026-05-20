package no.nav.dokdistavstemming.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("itest")
@EnableConfigurationProperties({
		DokdistavstemmingProperties.class,
		MqGatewayAlias.class,
		DokdistavstemmingServiceuser.class,
		JiraAuthProperties.class,
		SlackProperties.class,
		AzureProperties.class
})
@Import({
		JmsItestConfig.class
})
@EnableAutoConfiguration
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
public class ApplicationTestConfig {
}
