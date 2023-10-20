package no.nav.dokdistavstemming.consumer.jira.domain;

public record Status(String self,
					 String description,
					 String name,
					 String id,
					 StatusCategory statusCategory) {
}
