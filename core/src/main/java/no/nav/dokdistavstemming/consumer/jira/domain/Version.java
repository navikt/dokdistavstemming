package no.nav.dokdistavstemming.consumer.jira.domain;

public record Version(String self,
					  String id,
					  String description,
					  String name,
					  Boolean archived,
					  Boolean released,
					  Boolean overdue,
					  String userReleaseDate,
					  String projectId,
					  String userStartDate) {
}
