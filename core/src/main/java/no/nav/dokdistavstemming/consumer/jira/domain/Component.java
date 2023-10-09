package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.Builder;

import java.util.Map;

@Builder
public record Component(String self,
						String id,
						String name,
						String description,
						Map<String, String> lead,
						String displayName,
						Boolean active,
						Boolean isAssigneeTypeValid) {
}
