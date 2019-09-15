package no.nav.dokdistavstemming.consumer.jira;


import com.pep1.jira.client.domain.issue.Issue;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class JiraRequest {


	private IssueFields issueFields;
	private Issue issue;


	@Data
	@Builder
	public static class IssueFields {


	}

}
