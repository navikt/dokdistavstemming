package no.nav.dokdistavstemming.consumer.jira.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project {
	private String expand;
	private String self;
	private String id;
	private String key;
	private String describtion;
	private String name;
	private String url;
	private List<Component> components;
	private List<IssueType> issueTypes;
	private List<Version> versions;
}
