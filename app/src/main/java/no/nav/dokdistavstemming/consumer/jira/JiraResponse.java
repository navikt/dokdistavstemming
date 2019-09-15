package no.nav.dokdistavstemming.consumer.jira;


import com.pep1.jira.client.domain.issue.IssueFields;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JiraResponse {

	private String id;
	private String self;
	private String key;
	private IssueFields fields = new IssueFields();


}
