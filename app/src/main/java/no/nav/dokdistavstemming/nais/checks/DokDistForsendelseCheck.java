package no.nav.dokdistavstemming.nais.checks;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.nais.selftest.AbstractDependencyCheck;
import no.nav.dokdistavstemming.nais.selftest.ApplicationNotReadyException;
import no.nav.dokdistavstemming.nais.selftest.DependencyType;
import no.nav.dokdistavstemming.nais.selftest.Importance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class DokDistForsendelseCheck extends AbstractDependencyCheck {

	private final RestTemplate restTemplate;

	@Autowired
	public DokDistForsendelseCheck(@Value("${administrerforsendelse.v1.url}") String administrerforsendelseV1Url,
								   MeterRegistry meterRegistry, RestTemplateBuilder restTemplateBuilder, final ServiceuserAlias serviceuserAlias) {
		super(DependencyType.REST,"dokumentdistribusjon",administrerforsendelseV1Url, Importance.WARNING,meterRegistry);
		this.restTemplate = restTemplateBuilder
				.rootUri(administrerforsendelseV1Url)
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();
	}

	@Override
	protected void doCheck() {
		try {
			restTemplate.getForEntity("/ping",Object.class);
		} catch (Exception e){
			throw  new ApplicationNotReadyException("Kunne ikke pinge administrerforsendelse {dokumentdistribusjon}",e);
		}
	}
}
