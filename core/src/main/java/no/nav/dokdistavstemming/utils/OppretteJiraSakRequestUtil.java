package no.nav.dokdistavstemming.utils;


import no.nav.dokdistavstemming.consumer.jira.domain.BasicInputFields;
import no.nav.dokdistavstemming.consumer.jira.domain.Component;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueInput;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueType;
import no.nav.dokdistavstemming.consumer.jira.domain.Project;
import no.nav.dokdistavstemming.consumer.jira.domain.Reporter;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;

public class OppretteJiraSakRequestUtil {

	private static final String DESCRIPTION = "Se vedlegg for oversikt over dokumenter/brev som skulle ha fått «ekspedert» kvittering status.";
	private static final String ISSUE_TYPE_OPPGAVE = "Oppgave";
	private static final String COMPONENT_NAVN = "Dokumentdistribusjon";

	private OppretteJiraSakRequestUtil() {
	}

	public static IssueInput createJiraSaksRequest(Project project, String title, int avvikSize) {

		Component component = Component.builder()
				.name(COMPONENT_NAVN)
				.build();

		Project newProject = Project.builder()
				.key(project.key())
				.name(project.name())
				.components(singletonList(component))
				.build();

		IssueType newIssueType = project.issueTypes().stream()
				.filter(issueType1 -> ISSUE_TYPE_OPPGAVE.equals(issueType1.name()))
				.map(issueType -> issueType.withDescription(DESCRIPTION))
				.findFirst()
				.orElse(null);

		String[] labels = {"dokumentdistribusjon_avvik"};
		Reporter reporter = Reporter.builder()
				.name("srvjiradokdistavstemming")
				.key("srvjiradokdistavstemming")
				.build();

		BasicInputFields issueFields = BasicInputFields.builder()
				.project(newProject)
				.issuetype(newIssueType)
				.reporter(reporter)
				.labels(labels)
				.summary(String.format("Dokumentdistribusjon Kanal-%s: Utsendelse av %s dokumenter/brev har ikke mottatt kvittering", title, avvikSize))
				.description(String.format("Se vedlegg for oversikt over %s dokumenter/brev som skulle ha fått «ekspedert» kvittering status.", avvikSize))
				.build();
		return new IssueInput(issueFields);
	}
}
