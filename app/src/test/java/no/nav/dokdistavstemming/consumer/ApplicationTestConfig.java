package no.nav.dokdistavstemming.consumer;


import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("itest,wiremock")
@EnableConfigurationProperties({ServiceuserAlias.class})
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
public class ApplicationTestConfig {

	@Bean
	public ServiceuserAlias serviceuserAlias(){return mock(ServiceuserAlias.class);}

}
