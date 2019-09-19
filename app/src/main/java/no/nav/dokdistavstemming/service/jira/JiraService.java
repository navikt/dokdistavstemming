package no.nav.dokdistavstemming.service.jira;


import com.pep1.jira.client.domain.issue.Attachment;
import com.pep1.jira.client.domain.issue.Component;
import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.IssueType;
import com.pep1.jira.client.domain.issue.Priority;
import com.pep1.jira.client.domain.issue.Reporter;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class JiraService {

	private static ServiceuserAlias serviceuserAlias;
	private static final String BROWSE = "/browse";

	private static final String FEILMELDING = "En feil oppsto. Bestilling kan ikke utføres.";
	private JiraConsumer jiraConsumer;

	public JiraService(JiraConsumer jiraConsumer) {
		this.jiraConsumer = jiraConsumer;
	}


	public void createSakJira(){

		try {
			jiraConsumer.oppretteJiraSak(createJiraIssueRequest());

		} catch (DokDistAvstemmingFunctionalException e){
			throw new DokDistAvstemmingFunctionalException(String.format("Dokdistavstemming feilet til å opprette jirasak med feilmelding=%s",e.getMessage()));
		}
	}


	private IssueInput createJiraIssueRequest() {
		IssueInput issueInput = new IssueInput();

		Project project = new Project();
		project.setKey("MMA");
		project.setName("Team Dokument");

		Component component = new Component();
		component.setName("dokdistfordeling");

		Reporter reporter = new Reporter();
		reporter.setDisplayName("${spring.application.name}");
		IssueType issueType = new IssueType();
		issueType.setDescription("");
		issueType.setName("");


		Priority priority = new Priority();
		priority.setName("Medium");

		IssueFields issueFields = IssueFields.builder()
				.project(project)
				.labels(new String[]{"dokdistavstemming"})
				.components(Collections.singletonList(component))
				.reporter(reporter)
				.issuetype(issueType)
				.summary("Endre PDF sammeligning rammeverk til Apache PDFBox")
				.priority(priority)
				.fixVersions(new String[]{"dokdistavstemming"})
				.build();
		issueInput.setFields(issueFields);
		return issueInput;

	}



}
