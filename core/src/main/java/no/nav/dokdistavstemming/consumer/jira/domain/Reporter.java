package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Reporter {
	private String key;
	private String name;
	private String self;
}
