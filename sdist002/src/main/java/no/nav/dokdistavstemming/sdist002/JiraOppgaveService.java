package no.nav.dokdistavstemming.sdist002;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.jiraapi.JiraRequest;
import no.nav.dok.jiraapi.JiraResponse;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dok.jiracore.exception.JiraClientException;
import no.nav.dok.jiracore.exception.JiraServerException;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.JiraFunctionalException;
import no.nav.dokdistavstemming.exceptions.JiraTechnicalException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@Component
public class JiraOppgaveService {

	private static final DateTimeFormatter NORSK_LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	private static final String DESCRIPTION = "Se vedlegg for oversikt over %s dokumenter/brev som skulle ha fått «ekspedert» kvittering status.";
	private static final String SUMMARY = "Dokumentdistribusjon Kanal-%s: Utsendelse av %s dokumenter/brev har ikke mottatt kvittering";
	private static final String AVVIK_CSV_FILNAVN = "dokumentdistribusjon_avvik-%s-%s.csv";
	private static final String DOKDISTAVSTEMMING_JIRA_BRUKER_NAVN = "srvjiradokdistavstemming";
	private final JiraService jiraService;

	public JiraOppgaveService(JiraService jiraService) {
		this.jiraService = jiraService;
	}

	public JiraSakResponseTo opprettJirasak(DistribusjonKanalCode distribusjonKanal, byte[] csv, int size, LocalDate avstemmingsfilDato) {
		try {
			JiraRequest jiraRequest = mapJiraRequest(distribusjonKanal, size, csv, avstemmingsfilDato);

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

	private JiraRequest mapJiraRequest(DistribusjonKanalCode distribusjonskanal, int avvikSize, byte[] file, LocalDate avstemmingsfilDato) {
		return JiraRequest.builder()
				.summary(format(SUMMARY, distribusjonskanal.name(), avvikSize))
				.description(format(DESCRIPTION, avvikSize))
				.reporterName(DOKDISTAVSTEMMING_JIRA_BRUKER_NAVN)
				.labels(List.of("dokumentdistribusjon_avvik"))
				.filnavn(format(AVVIK_CSV_FILNAVN, distribusjonskanal.name(), NORSK_LOCAL_DATE_FORMAT.format(avstemmingsfilDato)))
				.vedlegg(file)
				.build();
	}
}
