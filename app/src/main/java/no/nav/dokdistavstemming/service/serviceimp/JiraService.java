package no.nav.dokdistavstemming.service.serviceimp;


import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.IssueType;
import com.pep1.jira.client.domain.issue.Priority;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.mdc.MDCConstants;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class JiraService {

	private static final String BROWSE = "/browse";
	private JiraConsumer jiraConsumer;
	private AvstemForsendelseService avstemForsendelseService;
	private MeterRegistry meterRegistry;

	public JiraService(JiraConsumer jiraConsumer, AvstemForsendelseService avstemForsendelseService, MeterRegistry meterRegistry) {
		this.jiraConsumer = jiraConsumer;
		this.avstemForsendelseService = avstemForsendelseService;
		this.meterRegistry = meterRegistry;
	}

	@Monitor(value = "dokdist_request", extraTags = {"process_code", "oppretteMMAJiraSak"}, percentiles = {0.5, 0.95})
	public JiraSakResponseTo oppretteMMAJiraSak() throws Exception {

		MDC.put(MDCConstants.MDC_REQUEST_ID, "oppretteMMAJiraSak");
		List<File> fils = avstemForsendelseService.henteDokDistFil();
		if (fils.isEmpty()) {
			log.info("Det fant ikke noen avvik fra dokumentdistribusjon(rdist002) og dokdistavstemming har ikke opprettet jira sak");
			return JiraSakResponseTo.builder()
					.message("Ingen filer og kan ikke opprette jira-sak")
					.httpStatusCode(HttpStatus.NO_CONTENT.value())
					.build();
		}
		IssueInput issueInput = createJiraSaksRequest();
		validateInput(issueInput);

		try {
			log.info(String.format("%s mottat kall til å opprette jira sak med vedlagge fra forskjellige distribusjonkanaler ", MDC.get(MDCConstants.MDC_REQUEST_ID)));
			Issue issue = jiraConsumer.oppretteJiraSak(issueInput);
			fils.forEach(fil -> jiraConsumer.leggVedlegg(issue.getKey(), fil));
			log.info(String.format("%s har opprettet MMA jira-sak med SaksId=%s SaksKey=%s self=%s",
					MDC.get(MDCConstants.MDC_REQUEST_ID), issue.getId(), issue.getKey(), issue.getSelf()));
			JiraSakResponseTo jiraSakResponseTo = JiraSakResponseTo.builder()
					.message(issue == null ? "Opprettet ikke jira sak" : String.format("%s%s/%s", getHostFraUrl(issue.getSelf()), BROWSE, issue.getKey()))
					.build();
			meterRegistry.counter("opprettet_jira",
					"jiraSakUrl", jiraSakResponseTo.getMessage() == null ? "Ukjent" : jiraSakResponseTo.getMessage())
					.increment();

			log.info(String.format("DokDistAvstemming opprettet jira sak med url=%s", jiraSakResponseTo.getMessage()));
			return jiraSakResponseTo;

		} catch (AvstemForsendelseFunctionalException e) {
			log.warn(String.format("%s feilet til å opprette jirasak, En eller flere nødvendige felter i metadata er null eller ugyldig feilmelding=%s", MDC.get(MDCConstants.MDC_REQUEST_ID),
					e.getMessage()));
			throw new AvstemForsendelseFunctionalException(String.format("%s feilet til å opprette jirasak, En eller flere nødvendige felter i metadata er null eller ugyldig feilmelding=%s", MDC.get(MDCConstants.MDC_REQUEST_ID),
					e.getMessage()));
		}
	}

	private void validateInput(IssueInput issueInput) {
		if (!isGyldigInput(issueInput)) {
			log.error(String.format("En eller flere nødvendige felter mangler eller er null. projectKey=%s, saksTypeNavn=%s",
					issueInput.getFields().getProject().getKey(), issueInput.getFields().getIssuetype().getName()));
			throw new AvstemForsendelseFunctionalException(String.format("Bestilling kan ikke utføres, nødvendige felter i mangler eller er null. projectKey=%s, saksTypeNavn=%s",
					issueInput.getFields().getProject().getKey(), issueInput.getFields().getIssuetype().getName()));
		}
	}

	private boolean isGyldigInput(IssueInput issueInput) {
		return !issueInput.getFields().getProject().getKey().isEmpty() && !issueInput.getFields().getIssuetype().getName().isEmpty();
	}


	private IssueInput createJiraSaksRequest() {
		IssueInput issueInput = new IssueInput();
		Project project = jiraConsumer.hentProjekt("MMA");
		List<com.pep1.jira.client.domain.issue.Component> componenter = project.getComponents().stream()
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
				.summary("DOKUMENTDISTRIBUSJON: Utsendelse av dokumenter/brev har ikke mottatt kvittering")
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
				throw new MalformedURLException(String.format("Fant ikke host url med feilmelding=%s", e.getMessage()));
			} catch (MalformedURLException ex) {
				log.error(String.format("Fant ikke host url med feilmelding=%s", ex.getMessage()));
			}
		}
		return hostFraUrl;
	}
}
