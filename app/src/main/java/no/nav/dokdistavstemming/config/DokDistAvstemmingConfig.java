package no.nav.dokdistavstemming.config;


import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.service.serviceimp.CSVProdusereImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DokDistAvstemmingConfig {

	@Bean
	public CSVProdusere csvProdusere() {
		return new CSVProdusereImpl();
	}
}
