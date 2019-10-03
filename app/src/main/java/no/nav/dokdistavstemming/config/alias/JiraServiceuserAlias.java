package no.nav.dokdistavstemming.config.alias;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotEmpty;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Getter
@Setter
@Configuration
@ConfigurationProperties(value = "jira")
public class JiraServiceuserAlias {

	@NotEmpty
	private String username;
	@NotEmpty
	private String password;
	private String host;
}
