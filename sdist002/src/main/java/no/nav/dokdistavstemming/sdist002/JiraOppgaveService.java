package no.nav.dokdistavstemming.sdist002;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.jiraapi.JiraRequest;
import no.nav.dok.jiraapi.JiraResponse;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dok.jiracore.exception.JiraClientException;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.JiraFunctionalException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Component
@Slf4j
public class JiraOppgaveService {

	private static final String DESCRIPTION = "Se vedlegg for oversikt over %s dokumenter/brev som skulle ha fått «ekspedert» kvittering status.";
	public static final String SUMMARY = "Dokumentdistribusjon Kanal-%s: Utsendelse av %s dokumenter/brev har ikke mottatt kvittering";
	private static final String DOKDISTAVSTEMMING_JIRA_BRUKER_NAVN = "srvjiradokdistavstemming";
	private final JiraService jiraService;

	public JiraOppgaveService(JiraService jiraService) {
		this.jiraService = jiraService;
	}

	public JiraSakResponseTo opprettJirasak(String distribusjonKanal, File fil, int size) {
		if (!isFilExistOgNotNull(fil)) {
			log.error("sdist002 kan ikke opprette Jira-sak. Fant ingen csv-fil. Må undersøkes av utvikler");
			return JiraSakResponseTo.builder()
					.message("Kan ikke opprette Jira-sak. Fant ingen csv-fil")
					.httpStatusCode(NO_CONTENT.value())
					.build();
		}

		try {
			JiraRequest jiraRequest = mapJiraRequest(distribusjonKanal, size, fil);

			log.info("opprettJirasak har mottatt kall om å opprette Jira-sak");

			JiraResponse jiraResponse = jiraService.opprettJiraOppgaveVedVedlegg(jiraRequest);

			log.info("Sdist002 har opprettet og oppdatert jira oppgaven med med key={} til status={}", jiraResponse.jiraIssueKey(), jiraResponse.status());
			return JiraSakResponseTo.builder()
					.jiraSakKey(jiraResponse.jiraIssueKey())
					.message(jiraResponse.message())
					.httpStatusCode(HttpStatus.OK.value())
					.build();

		} catch (JiraFunctionalException | JiraClientException e) {
			log.warn("opprettJirasak kunne ikke opprette jirasak. Ett eller flere nødvendige felter i metadata er null, eller feil={}", e.getMessage());
			throw new JiraFunctionalException(format("opprettJirasak kunne ikke opprette jirasak. Ett eller flere nødvendige felter i metadata er null eller feil=%s",
					e.getMessage()));
		}
	}

	private JiraRequest mapJiraRequest(String title, int avvikSize, File file) {
		return JiraRequest.builder()
				.summary(format(SUMMARY, title, avvikSize))
				.description(format(DESCRIPTION, avvikSize))
				.reporterName(DOKDISTAVSTEMMING_JIRA_BRUKER_NAVN)
				.labels(List.of("dokumentdistribusjon_avvik"))
				.file(file)
				.build();
	}

	private boolean isFilExistOgNotNull(File fil) {
		return fil != null && fil.exists() && fil.length() > 0;
	}
}
