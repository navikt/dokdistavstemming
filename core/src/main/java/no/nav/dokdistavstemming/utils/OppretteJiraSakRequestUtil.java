package no.nav.dokdistavstemming.utils;


import no.nav.dokdistavstemming.consumer.jira.domain.Component;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueFields;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueInput;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueType;
import no.nav.dokdistavstemming.consumer.jira.domain.Priority;
import no.nav.dokdistavstemming.consumer.jira.domain.Project;
import no.nav.dokdistavstemming.consumer.jira.domain.Reporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OppretteJiraSakRequestUtil {

	private static final String DESCRIPTION = "Se vedlegg for oversikt over dokumenter/brev som skulle ha fått «ekspedert» kvittering status.";

	private OppretteJiraSakRequestUtil() {
	}

	public static IssueInput createJiraSaksRequest(Project project, String title, int avvikSize) {

		List<Component> componenter = project.getComponents().stream()
				.filter(dokdistComp -> dokdistComp.name().equalsIgnoreCase("Dokumentdistribusjon"))
				.toList();
		project.setComponents(componenter);

		IssueType issueType = project.getIssueTypes().stream()
				.filter(issueType1 -> "Oppgave".equals(issueType1.name()))
				.findFirst()
				.orElse(null);

		issueType.withDescription(DESCRIPTION);

		String[] labels = {"dokumentdistribusjon_avvik"};
		Reporter reporter = new Reporter("srvjiradokdistavstemming", "srvjiradokdistavstemming", null);
		Priority priority = new Priority("Viktig");

		String[] customObject = {"Dokumentdistribusjon (CMDB-31953)"};
		Map<String, Object> custemField = new HashMap<>();

		custemField.put("customfield_20211", customObject);

		IssueFields issueFields = IssueFields.builder()
				.project(project)
				.issuetype(issueType)
				.reporter(reporter)
				.labels(labels)
				.summary(String.format("Dokumentdistribusjon Kanal-%s: Utsendelse av %s dokumenter/brev har ikke mottatt kvittering", title, avvikSize))
				.description(String.format("Se vedlegg for oversikt over %s dokumenter/brev som skulle ha fått «ekspedert» kvittering status.", avvikSize))
				.build();

		return new IssueInput(issueFields);
	}
}
