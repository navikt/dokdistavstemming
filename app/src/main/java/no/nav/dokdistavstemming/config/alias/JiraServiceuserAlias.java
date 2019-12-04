package no.nav.dokdistavstemming.config.alias;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
public class JiraServiceuserAlias {

	@NotEmpty
	private String username;
	@NotEmpty
	private String password;
}
