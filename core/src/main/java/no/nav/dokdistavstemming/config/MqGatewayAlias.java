package no.nav.dokdistavstemming.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Getter
@Setter
@ToString
@ConfigurationProperties("mqgateway01")
@Validated
public class MqGatewayAlias {
	@NotEmpty
	private String hostname;
	@NotEmpty
	private String name;
	@Positive
	private int port;
	private MqChannel channel = new MqChannel();

	@Data
	@Validated
	public static class MqChannel {
		@NotEmpty
		private String name;
		@NotBlank
		private String securename;
		private boolean enabletls;
	}
}
