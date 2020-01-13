package no.nav.dokdistavstemming.utils;

import com.pep1.jira.client.domain.issue.Component;
import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.IssueType;
import com.pep1.jira.client.domain.issue.Priority;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;

import java.util.List;
import java.util.stream.Collectors;

public class OppretteJiraSakRequestUtil {

	private OppretteJiraSakRequestUtil(){}

	public static  IssueInput createJiraSaksRequest(Project project, String title) {
		IssueInput issueInput = new IssueInput();
		List<Component> componenter = project.getComponents().stream()
				.filter(dokdistComp -> dokdistComp.getName().equalsIgnoreCase("Dokumentdistribusjon"))
				.collect(Collectors.toList());

		IssueType issueType = new IssueType();
		issueType.setDescription("Se i vedlegg oversikten av dokumenter/brev som skulle ha fått «ekspedert» kvittering status.");
		issueType.setName("Oppgave");

		Priority priority = new Priority();
		priority.setName("Medium");

		IssueFields issueFields = IssueFields.builder()
				.project(project)
				.issuetype(issueType)
				.components(componenter)
				.summary(String.format("DOKUMENTDISTRIBUSJON Kanal-%s: Utsendelse av dokumenter/brev har ikke mottatt kvittering",title))
				.description("Se i vedlegg oversikten av dokumenter/brev som skulle ha fått «ekspedert» kvittering status.")
				.priority(priority)
				.build();
		issueInput.setFields(issueFields);
		return issueInput;

	}
}
