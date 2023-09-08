package no.nav.dokdistavstemming.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Getter
@Setter
@ToString
@Validated
@ConfigurationProperties("mqgateway01")
public class MqGatewayAlias {
	@Positive
	private int port;
	@NotEmpty
	private String name;
	@NotEmpty
	private String hostname;
	@NotEmpty
	private String channelName;

}


