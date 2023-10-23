package no.nav.dokdistavstemming.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("mqgateway01")
public record MqGatewayAlias(
		@Positive int port,
		@NotEmpty String name,
		@NotEmpty String hostname,
		@NotEmpty String channelname) {
}


