package no.nav.dokdistavstemming.sdist002;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.consumer.jira.domain.Issue;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueInput;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.domain.to.JiraTransition;
import no.nav.dokdistavstemming.exceptions.JiraFunctionalException;
import no.nav.dokdistavstemming.utils.OppretteJiraSakRequestUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Component
@Slf4j
public class JiraService {

	private static final String BROWSE = "/browse";
	private static final String TRANSITION_ID = "121";
	private final JiraConsumer jiraConsumer;

	public JiraService(JiraConsumer jiraConsumer) {
		this.jiraConsumer = jiraConsumer;
	}

	public JiraSakResponseTo opprettJirasak(String distribusjonKanal, File fil, int size) {
		if (!isFilExistOgNotNull(fil)) {
			log.error("sdist002 kan ikke opprette Jira-sak. Fant ingen csv-fil. Må undersøkes av utvikler");
			return JiraSakResponseTo.builder()
					.message("Kan ikke opprette Jira-sak. Fant ingen csv-fil")
					.httpStatusCode(NO_CONTENT.value())
					.build();
		}

		IssueInput issueInput = OppretteJiraSakRequestUtil.createJiraSaksRequest(jiraConsumer.hentProsjekt("MMA"), distribusjonKanal, size);
		validateInput(issueInput);

		try {
			log.info("Oppretter Jira-sak for distribusjonKanal={}", distribusjonKanal);
			Issue issue = jiraConsumer.opprettJiraSak(issueInput);
			jiraConsumer.leggTilVedlegg(issue.getKey(), fil);
			log.info("Opprettet Jira-sak med key={} for distribusjonKanal={}", issue.getKey(), distribusjonKanal);
			updateJiraStatus(issue);
			return JiraSakResponseTo.builder()
					.jiraSakKey(issue.getKey())
					.message(format("%s%s/%s", getHostFraUrl(issue.getSelf()), BROWSE, issue.getKey()))
					.build();
		} catch (JiraFunctionalException e) {
			log.warn("Kunne ikke opprette jirasak. Ett eller flere nødvendige felter i metadata er null, eller feil={}", e.getMessage());
			throw new JiraFunctionalException(format("Kunne ikke opprette jirasak. Ett eller flere nødvendige felter i metadata er null eller feil=%s",
					e.getMessage()));
		}
	}

	private void updateJiraStatus(Issue issue) {
		Issue updateIssue = jiraConsumer.oppdaterStatus(issue.getKey(), JiraTransition.builder()
				.transition(JiraTransition.Transition.builder().id(TRANSITION_ID).build()).build());
		log.info("Oppdatert Jira-sak med key={} til status={}", issue.getKey(), updateIssue.getFields().getStatus().name());
	}

	private void validateInput(IssueInput issueInput) {
		if (!isGyldigInput(issueInput)) {
			log.error("Ett eller flere nødvendige felter mangler eller er null. projectKey={}, saksTypeNavn={}",
					issueInput.fields().getProject().key(), issueInput.fields().getIssuetype().name());
			throw new JiraFunctionalException(format("Bestilling kan ikke utføres. Nødvendige felter mangler eller er null. projectKey=%s, saksTypeNavn=%s",
					issueInput.fields().getProject().key(), issueInput.fields().getProject().name()));
		}
	}

	private boolean isGyldigInput(IssueInput issueInput) {
		return !issueInput.fields().getProject().key().isEmpty() && !issueInput.fields().getProject().name().isEmpty();
	}

	private boolean isFilExistOgNotNull(File fil) {
		return fil != null && fil.exists() && fil.length() > 0;
	}

	private String getHostFraUrl(String stringUrl) {
		String hostFraUrl = "";
		try {
			URL url = new URL(stringUrl);
			hostFraUrl = url.getProtocol() + "://" + url.getHost();
		} catch (MalformedURLException e) {
			try {
				throw new MalformedURLException(format("Fant ikke host url med feilmelding=%s", e.getMessage()));
			} catch (MalformedURLException ex) {
				log.error("Fant ikke host url med feilmelding={}", ex.getMessage());
			}
		}
		return hostFraUrl;
	}
}
