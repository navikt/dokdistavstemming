package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.config.AzureProperties;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.DokdistavstemmingServiceuser;
import no.nav.dokdistavstemming.config.MqGatewayAlias;
import no.nav.dokdistavstemming.config.OAuth2WebClientConfig;
import no.nav.dokdistavstemming.sdist002.Sdist002Scheduler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import static java.lang.System.getenv;
import static java.lang.System.setProperty;


@EnableScheduling
@EnableConfigurationProperties(value = {
		DokdistavstemmingProperties.class,
		MqGatewayAlias.class,
		DokdistavstemmingServiceuser.class,
		AzureProperties.class
})
@Import(value = {
		OAuth2WebClientConfig.class,
		CoreConfig.class,
		Sdist002Scheduler.class,
		Sdist004Scheduler.class,
		Sdist006Scheduler.class
})
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		setProperty("javax.net.ssl.keyStorePassword", getenv("DOKDISTAVSTEMMINGCERT_KEYSTORE_PASSWORD"));
		SpringApplication.run(Application.class, args);
	}
}
