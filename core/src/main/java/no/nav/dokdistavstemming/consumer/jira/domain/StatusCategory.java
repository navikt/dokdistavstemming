package no.nav.dokdistavstemming.consumer.jira.domain;

public record StatusCategory(String self,
							 String id,
							 String key,
							 String colorName,
							 String name) {
}
