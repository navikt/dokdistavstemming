package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.azure.AzureConfig;
import no.nav.dokdistavstemming.config.AvstemForsendelseConfig;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.WebClientConfig;
import no.nav.dokdistavstemming.scheduler.Sdist002ScheduleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@EnableConfigurationProperties(value = {
		DokdistavstemmingProperties.class,
		AzureConfig.class
})
@Import(value = {
		WebClientConfig.class,
		Sdist002ScheduleConfig.class,
		AvstemForsendelseConfig.class,
		CoreConfig.class,
		Sdist004Config.class
})
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
