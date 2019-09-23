package no.nav.dokdistavstemming.service.jira;


import com.pep1.jira.client.domain.issue.Component;
import com.pep1.jira.client.domain.issue.Issue;
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
import no.nav.dokdistavstemming.mdc.MDCConstants;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class JiraService {

	private static final String BROWSE = "/browse";
	private static final String FEILMELDING = "En feil oppsto. Bestilling kan ikke utføres.";
	private static ServiceuserAlias serviceuserAlias;
	private JiraConsumer jiraConsumer;

	public JiraService(JiraConsumer jiraConsumer) {
		this.jiraConsumer = jiraConsumer;
	}

	@Monitor(value = "dokdist_request", extraTags = {"process_code", "createJiraSak"}, percentiles = {0.5, 0.95})
	public void createJiraSak() {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "createJiraSak");
		try {
			log.info(String.format("%s mottat kall til å opprette jira sak med vedlagge ",MDC.get(MDCConstants.MDC_REQUEST_ID)));
			Issue issue=jiraConsumer.oppretteJiraSak(createJiraIssueRequest());
			log.info(String.format("%s /n %s /n %s",issue.getId(),issue.getKey(),issue.getSelf()));

		} catch (DokDistAvstemmingFunctionalException e) {
			log.warn(String.format("%s ",e.getMessage()));
			throw new DokDistAvstemmingFunctionalException(String.format("Dokdistavstemming feilet til å opprette jirasak med feilmelding=%s",
					e.getMessage()));
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
		IssueType issueType = new IssueType();
		issueType.setDescription("Represents a Test");
		issueType.setName("Test");


		Priority priority = new Priority();
		priority.setName("Medium");

		IssueFields issueFields = IssueFields.builder()
				.project(project)
				.issuetype(issueType)
				.summary("Endre PDF sammeligning rammeverk til Apache PDFBox")
				.priority(priority)
				.build();
		issueInput.setFields(issueFields);
		return issueInput;

	}


}
