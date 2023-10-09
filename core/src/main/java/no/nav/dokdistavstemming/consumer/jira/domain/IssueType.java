package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.Builder;

@Builder
public record IssueType(String self,
						String id,
						String description,
						String iconUrl,
						String name,
						Boolean subtask,
						Integer avatarId) {


	public IssueType withDescription(String description) {
		return new IssueType(self(), id(), description, iconUrl(), name(), subtask(), avatarId());
	}
}
