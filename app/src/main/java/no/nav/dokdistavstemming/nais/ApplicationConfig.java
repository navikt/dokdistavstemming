package no.nav.dokdistavstemming.nais;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.metrics.DokMonitoringAspect;
import no.nav.dokdistavstemming.scheduler.LeaderElection;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.service.DokDistAvstemmingService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


@ComponentScan(basePackages = "no.nav.dokdistavstemming")
@Configuration
@EnableConfigurationProperties(value = {ServiceuserAlias.class})
@EnableAspectJAutoProxy
@EnableRetry
@EnableScheduling
public class ApplicationConfig {

	@Bean
	public DokMonitoringAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokMonitoringAspect(meterRegistry);
	}



}
