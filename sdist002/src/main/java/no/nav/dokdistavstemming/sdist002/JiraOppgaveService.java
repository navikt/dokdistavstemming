package no.nav.dokdistavstemming.sdist002;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.jiraapi.JiraRequest;
import no.nav.dok.jiraapi.JiraResponse;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dok.jiracore.exception.JiraClientException;
import no.nav.dok.jiracore.exception.JiraServerException;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.JiraFunctionalException;
import no.nav.dokdistavstemming.exceptions.JiraTechnicalException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@Component
public class JiraOppgaveService {

	private static final String DESCRIPTION = "Se vedlegg for oversikt over %s dokumenter/brev som skulle ha fått «ekspedert» kvittering status.";
	public static final String SUMMARY = "Dokumentdistribusjon Kanal-%s: Utsendelse av %s dokumenter/brev har ikke mottatt kvittering";
	private static final String DOKDISTAVSTEMMING_JIRA_BRUKER_NAVN = "srvjiradokdistavstemming";
	private final JiraService jiraService;

	public JiraOppgaveService(JiraService jiraService) {
		this.jiraService = jiraService;
	}

	public JiraSakResponseTo opprettJirasak(String distribusjonKanal, byte[] csv, int size) {
		try {
			JiraRequest jiraRequest = mapJiraRequest(distribusjonKanal, size, csv);

			log.info("opprettJirasak har mottatt kall om å opprette Jira-sak");

			JiraResponse jiraResponse = jiraService.opprettJiraMMAOppgaveMedVedlegg(jiraRequest);

			log.info("Sdist002 har opprettet og oppdatert jira oppgaven med key={}, url={}", jiraResponse.jiraIssueKey(), jiraResponse.message());
			return JiraSakResponseTo.builder()
					.jiraSakKey(jiraResponse.jiraIssueKey())
					.message(jiraResponse.message())
					.httpStatusCode(CREATED.value())
					.build();

		} catch (JiraClientException | ResourceAccessException e) {
			log.warn(e.getMessage());
			throw new JiraFunctionalException(e.getMessage(), e);
		} catch (JiraServerException e) {
			throw new JiraTechnicalException(e.getMessage(), e);
		}
	}

	private JiraRequest mapJiraRequest(String title, int avvikSize, byte[] file) {
		return JiraRequest.builder()
				.summary(format(SUMMARY, title, avvikSize))
				.description(format(DESCRIPTION, avvikSize))
				.reporterName(DOKDISTAVSTEMMING_JIRA_BRUKER_NAVN)
				.labels(List.of("dokumentdistribusjon_avvik"))
				.vedlegg(file)
				.build();
	}
}
