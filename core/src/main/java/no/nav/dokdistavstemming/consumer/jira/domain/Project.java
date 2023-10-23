package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record Project(String expand,
					  String self,
					  String id,
					  String key,
					  String description,
					  String name,
					  String url,
					  List<Component> components,
					  List<IssueType> issueTypes,
					  List<Version> versions) {

}
