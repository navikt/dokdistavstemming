package no.nav.dokdistavstemming;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.alias.JiraServiceuserAlias;
import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.nais.ApplicationConfig;
import no.nav.dokdistavstemming.utils.RequestBuilder;
import org.mockito.Mock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import javax.inject.Inject;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@Profile("itest,wiremock")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {ApplicationConfig.class})
@EnableConfigurationProperties({ServiceuserAlias.class})
@ComponentScan(basePackages = "no.nav.dokdistavstemming")
public abstract class AbstractIT {

	@Inject
	protected TestRestTemplate testRestTemplate;
	@Inject
	protected JiraServiceuserAlias jiraServiceuserAlias;

	@Mock
	private MeterRegistry meterRegistry;

	@Mock
	private Counter counterMock;

	protected RequestBuilder createRequestWithBasicHeader() {
		return new RequestBuilder().basicHeader(jiraServiceuserAlias.getUsername(),jiraServiceuserAlias.getPassword());

	}

}
