package no.nav.dokdistavstemming.service.jira;


import com.pep1.jira.client.domain.issue.Attachment;
import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class JiraService {

	private static final String BROWSE = "/browse";

	private static final String FEILMELDING = "En feil oppsto. Bestilling kan ikke utføres.";


	private IssueInput createJiraIssueRequest(List<Attachment> attachmentList) {
		IssueInput issueInput = new IssueInput();
		Project project = new Project();
		project.setName("FAGSYSTEM");
		project.setKey("FAGSYSTEM-");
		IssueFields issueFields = IssueFields.builder()
				.project(project)
				.attachment(attachmentList)
				.created(DateTime.now())
				.build();
		issueInput.setFields(issueFields);
		return issueInput;

	}


}
