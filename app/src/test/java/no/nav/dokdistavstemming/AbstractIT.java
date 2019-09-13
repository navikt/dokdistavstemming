package no.nav.dokdistavstemming;


import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.nais.ApplicationConfig;
import no.nav.dokdistavstemming.service.DokDistAvstemmingService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;


@Profile("itest,wiremock")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = { ApplicationConfig.class})
@EnableConfigurationProperties({ServiceuserAlias.class})
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
public abstract class AbstractIT {


}
