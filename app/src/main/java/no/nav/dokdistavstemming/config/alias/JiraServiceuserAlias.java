package no.nav.dokdistavstemming.config.alias;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jira")
public class JiraServiceuserAlias {

	private String username;
	private String password;
	private String host;
}
