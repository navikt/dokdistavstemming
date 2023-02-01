package no.nav.dokdistavstemming.config;


import no.nav.dokdistavstemming.sdist002.CSVProdusere;
import no.nav.dokdistavstemming.sdist002.serviceimp.CSVProdusereImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvstemForsendelseConfig {

	@Bean
	public CSVProdusere csvProdusere() {
		return new CSVProdusereImpl();
	}
}
