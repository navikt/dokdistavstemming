package no.nav.dokdistavstemming.consumer.jira;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JiraResponse {
	private String id;
	private String self;
	private String key;
}
