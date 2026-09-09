package no.nav.dokdistavstemming.sdist006;

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStopp;
import org.apache.avro.util.ClassSecurityValidator;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvroSecurityConfig {

	static {
		// Avro deserialiserer kun til klasser som er i en allowlist; alt som ikke er med her vil feile
		ClassSecurityValidator.setGlobal(ClassSecurityValidator.composite(
				ClassSecurityValidator.DEFAULT,
				ClassSecurityValidator.builder()
						.add(DoknotifikasjonStopp.class)
						.build()));
	}
}
