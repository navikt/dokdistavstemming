package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.config.AvstemForsendelseConfig;
import no.nav.dokdistavstemming.nais.ApplicationConfig;
import no.nav.dokdistavstemming.nais.NaisContract;
import no.nav.dokdistavstemming.scheduler.Sdist002ScheduleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(value = {
		ApplicationConfig.class,
		NaisContract.class,
		Sdist002ScheduleConfig.class,
		AvstemForsendelseConfig.class
})

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
