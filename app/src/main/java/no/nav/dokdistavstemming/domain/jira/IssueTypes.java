package no.nav.dokdistavstemming.domain.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueTypes {

	private String self;
	private String id;
	private String description;
	private String iconUrl;
	private String name;
	private Boolean subtask;
	private String expand;
	private Fields fields;
}
