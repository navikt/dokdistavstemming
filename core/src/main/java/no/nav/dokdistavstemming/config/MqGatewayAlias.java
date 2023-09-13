package no.nav.dokdistavstemming.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Validated
@ConfigurationProperties("mqgateway01")
public record MqGatewayAlias (
	@Positive int port,
	@NotEmpty String name,
	@NotEmpty String hostname,
	@NotEmpty String channelname){}


