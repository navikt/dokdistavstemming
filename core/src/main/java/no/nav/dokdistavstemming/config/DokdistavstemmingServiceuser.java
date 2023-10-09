package no.nav.dokdistavstemming.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString
@ConfigurationProperties("serviceuser")
@Validated
public class DokdistavstemmingServiceuser {
	@NotEmpty
	private String username;
	@NotEmpty
	private String password;
}
