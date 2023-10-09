package no.nav.dokdistavstemming.consumer.jira.domain;

import java.util.List;

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

	public Project addComponents(List<Component> components) {
		if (!components.isEmpty()) {
			return new Project(expand(), self(), id(), key(), description(), name(), url(), components, issueTypes(), versions());
		}
		return new Project(expand(), self(), id(), key(), description(), name(), url(), components(), issueTypes(), versions());
	}
}
