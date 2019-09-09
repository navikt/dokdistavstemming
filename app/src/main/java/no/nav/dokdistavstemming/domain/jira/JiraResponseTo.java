package no.nav.dokdistavstemming.domain.jira;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class JiraResponseTo implements JiraDTO{

	private Long sakId;
	private String issueName;
	private String jiraKey;
	private String keyWords;
	private String jiraUrl;
	private String status;


}
