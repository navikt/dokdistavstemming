package no.nav.dokdistavstemming.service.serviceimp;


import com.pep1.jira.client.domain.issue.Attachment;
import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.IssueType;
import com.pep1.jira.client.domain.issue.Priority;
import com.pep1.jira.client.domain.issue.Reporter;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;
import no.nav.dokdistavstemming.mdc.MDCConstants;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JiraService {

	private static final String BROWSE = "/browse";

	private JiraConsumer jiraConsumer;
	private DokDistAvstemmingService dokDistAvstemmingService;


	public JiraService(JiraConsumer jiraConsumer, DokDistAvstemmingService dokDistAvstemmingService) {
		this.jiraConsumer = jiraConsumer;
		this.dokDistAvstemmingService = dokDistAvstemmingService;
	}

	@Monitor(value = "dokdist_request", extraTags = {"process_code", "createJiraSak"}, percentiles = {0.5, 0.95})
	public JiraSakResponseTo createJiraSak() throws Exception {

		MDC.put(MDCConstants.MDC_REQUEST_ID, "createJiraSak");
		IssueInput issueInput = createJiraSaksRequest();
		validateInput(issueInput);

		try {
			List<File> fils = dokDistAvstemmingService.henteDokDistFil();
			log.info(String.format("%s mottat kall til å opprette jira sak med vedlagge ", MDC.get(MDCConstants.MDC_REQUEST_ID)));
			Issue issue = jiraConsumer.oppretteJiraSak(issueInput);
			List<Attachment> attachments = fils.stream().map(fil -> jiraConsumer.laggeVedlagg(issue.getKey(), fil))
					.collect(Collectors.toList());
			log.info(String.format("%s har opprettet MMA jira-sak med SaksId=%s SaksKey=%s self=%s",
					MDC.get(MDCConstants.MDC_REQUEST_ID), issue.getId(), issue.getKey(), issue.getSelf()));
			JiraSakResponseTo jiraSakResponseTo = JiraSakResponseTo.builder()
					.message(!issue.equals(null) ? String.format("%s%s/%s", getHostFraUrl(issue.getSelf()), BROWSE, issue.getKey()) : null)
					.build();

			log.info(String.format("DokDistAvstemming opprettet jira sak med url=%s", jiraSakResponseTo.getMessage()));
			return jiraSakResponseTo;

		} catch (DokDistAvstemmingFunctionalException e) {
			log.warn(String.format("%s til å opprette jirasak, En eller flere nødvendige felter i metadata er null eller ugyldig feilmelding=%s", MDC.get(MDCConstants.MDC_REQUEST_ID),
					e.getMessage()));
			throw new DokDistAvstemmingFunctionalException(String.format("%s til å opprette jirasak, En eller flere nødvendige felter i metadata er null eller ugyldig feilmelding=%s", MDC.get(MDCConstants.MDC_REQUEST_ID),
					e.getMessage()));
		}
	}


	private void validateInput(IssueInput issueInput) {
		if (!isGyldigInput(issueInput)) {
			log.error(String.format("En eller flere nødvendige felter mangler eller er null. projectKey=%s, saksTypeNavn=%s",
					issueInput.getFields().getProject().getKey(), issueInput.getFields().getIssuetype().getName()));
			throw new DokDistAvstemmingFunctionalException(String.format("Bestilling kan ikke utføres, nødvendige felter i mangler eller er null. projectKey=%s, saksTypeNavn=%s",
					issueInput.getFields().getProject().getKey(), issueInput.getFields().getIssuetype().getName()));
		}
	}

	private boolean isGyldigInput(IssueInput issueInput) {
		return !issueInput.getFields().getProject().getKey().isEmpty() && !issueInput.getFields().getIssuetype().getName().isEmpty();
	}


	public IssueInput createJiraSaksRequest() {
		IssueInput issueInput = new IssueInput();

		Project project = new Project();
		project.setKey("MMA");
		project.setName("Team Dokument");

		com.pep1.jira.client.domain.issue.Component component = new com.pep1.jira.client.domain.issue.Component();
		component.setName("DokDistAvstemming, DokumentDistribusjon");

		Reporter reporter = new Reporter();
		reporter.setDisplayName("DokDistAvstemming Applikajonen");
		IssueType issueType = new IssueType();
		issueType.setDescription("Se i vedlegg oversikten av dokumenter/brev som skulle ha fått «ekspedert» kvittering status.");
		issueType.setName("Test");
		Attachment attachment = new Attachment();
		File file = new File("__files/hentuekspedereforsendelse-empty.json");

		attachment.setFilename(file.getAbsoluteFile().getName());

		Priority priority = new Priority();
		priority.setName("Medium");

		IssueFields issueFields = IssueFields.builder()
				.project(project)
				.issuetype(issueType)
				.summary("DOKUMENTDISTRIBUSJON: Utsendelse av dokumenter/brev er forsinket")
				.description("Se i vedlegg oversikten av dokumenter/brev som skulle ha fått «ekspedert» kvittering status.")
				.priority(priority)
				.build();
		issueInput.setFields(issueFields);
		return issueInput;

	}

	private String getHostFraUrl(String stringUrl) {
		String hostFraUrl = "";
		try {

			URL url = new URL(stringUrl);
			hostFraUrl = url.getProtocol() + "://" + url.getHost();

		} catch (MalformedURLException e) {
			try {
				throw new MalformedURLException("");
			} catch (MalformedURLException ex) {
				ex.printStackTrace();
			}
		}
		return hostFraUrl;
	}

}
