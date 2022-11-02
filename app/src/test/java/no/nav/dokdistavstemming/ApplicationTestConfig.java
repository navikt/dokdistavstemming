package no.nav.dokdistavstemming;


import no.nav.dokdistavstemming.config.DokdistavstemmingProp;
import no.nav.dokdistavstemming.config.WebClientConfig;
import no.nav.dokdistavstemming.config.alias.ServiceuserProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("itest,wiremock")
@EnableConfigurationProperties({
		ServiceuserProperties.class,
		DokdistavstemmingProp.class,
		WebClientConfig.class
})
public class ApplicationTestConfig {

	@Bean
	public ServiceuserProperties serviceuserAlias(){return mock(ServiceuserProperties.class);}

	@Bean
	public DokdistavstemmingProp dokdistavstemmingProp(){return mock(DokdistavstemmingProp.class);}

}
