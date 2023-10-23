package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
	private String expand;
	private String id;
	private String self;
	private String key;
	private IssueFields fields;
	private Status status;
}
