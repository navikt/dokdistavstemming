package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.Builder;

@Builder
public record Component(String self,
						String id,
						String name,
						Boolean isAssigneeTypeValid) {
}
