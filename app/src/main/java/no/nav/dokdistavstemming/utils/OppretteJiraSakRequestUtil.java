package no.nav.dokdistavstemming.utils;

import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.IssueType;
import com.pep1.jira.client.domain.issue.Priority;
import com.pep1.jira.client.domain.issue.Reporter;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OppretteJiraSakRequestUtil {

	private OppretteJiraSakRequestUtil() {
	}

	public static IssueInput createJiraSaksRequest(Project project, String title, int avvikSize) {
		IssueInput issueInput = new IssueInput();
		List<com.pep1.jira.client.domain.issue.Component> componenter = project.getComponents().stream()
				.filter(dokdistComp -> dokdistComp.getName().equalsIgnoreCase("Dokumentdistribusjon"))
				.collect(Collectors.toList());
		project.setComponents(componenter);

		IssueType issueType = project.getIssueTypes().stream().filter(issueType1 -> "Oppgave".equals(issueType1.getName())).findFirst().get();
		issueType.setDescription("Se i vedlegg oversikten av dokumenter/brev som skulle ha fått «ekspedert» kvittering status.");

		String[] labels = {"dokumentdistribusjon_avvik"};
		Reporter reporter = new Reporter();

		reporter.setKey("srvjiradokdistavstemming");
		reporter.setName("srvjiradokdistavstemming");
		Priority priority = new Priority();
		priority.setName("Viktig");

		String[] customObject = {"Dokumentdistribusjon (CMDB-31953)"};
		Map<String, Object> custemField = new HashMap<>();

		custemField.put("customfield_20211", customObject);

		IssueFields issueFields = IssueFields.builder()
				.project(project)
				.issuetype(issueType)
				.reporter(reporter)
				.labels(labels)
				.summary(String.format("DOKUMENTDISTRIBUSJON Kanal-%s: Utsendelse av dokumenter/brev har ikke mottatt kvittering", title))
				.description(String.format("Se i vedlegg oversikten av %s dokumenter/brev som skulle ha fått «ekspedert» kvittering status.", avvikSize))
				.build();
		issueInput.setFields(issueFields);
		return issueInput;

	}
}
