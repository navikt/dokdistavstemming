package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.Builder;

@Builder
public record Issue(String expand,
					String id,
					String self,
					String key,
					IssueFields fields, Status status) {

}
