package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.config.DokDistAvstemmingConfig;
import no.nav.dokdistavstemming.nais.ApplicationConfig;
import no.nav.dokdistavstemming.nais.NaisContract;
import no.nav.dokdistavstemming.scheduler.DokDistAvstemmingScheduleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(value = {
		ApplicationConfig.class,
		NaisContract.class,
		DokDistAvstemmingScheduleConfig.class,
		DokDistAvstemmingConfig.class
})

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
