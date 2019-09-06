package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.nais.ApplicationConfig;
import no.nav.dokdistavstemming.nais.NaisContract;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(value = {
		ApplicationConfig.class,
		NaisContract.class

})

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
