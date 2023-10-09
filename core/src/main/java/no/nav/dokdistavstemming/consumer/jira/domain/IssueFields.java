package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record IssueFields(Project project,
						  String summary,
						  IssueType issuetype,
						  List<Component> components,
						  Reporter creator,
						  Reporter assignee,
						  Reporter reporter,
						  Priority priority,
						  String description,
						  String[] labels, Status status) {


}
