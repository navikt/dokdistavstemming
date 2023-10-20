package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasicInputFields {
	private Project project;
	private String summary;
	private IssueType issuetype;
	private Reporter reporter;
	private String description;
	private String[] labels;
}
