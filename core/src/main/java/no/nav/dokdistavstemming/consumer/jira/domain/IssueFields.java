package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueFields {
	private Project project;
	private String summary;
	private IssueType issuetype;
	private Reporter creator;
	private Reporter assignee;
	private Reporter reporter;
	private Priority priority;
	private String description;
	private String[] labels;
	private Status status;
}
