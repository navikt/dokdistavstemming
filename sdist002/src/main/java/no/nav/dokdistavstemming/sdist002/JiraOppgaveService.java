package no.nav.dokdistavstemming.sdist002;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.jiraapi.JiraRequest;
import no.nav.dok.jiraapi.JiraResponse;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dok.jiracore.exception.JiraClientException;
import no.nav.dok.jiracore.exception.JiraServerException;
import no.nav.dokdistavstemming.config.JiraAuthProperties;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.exceptions.JiraFunctionalException;
import no.nav.dokdistavstemming.exceptions.JiraTechnicalException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@Service
public class JiraOppgaveService {

	private static final DateTimeFormatter NORSK_LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	private static final String DESCRIPTION = "Se vedlegg for oversikt over %s dokumenter/brev som skulle ha fått «ekspedert» kvittering status fra utsendingskanal %s.";
	private static final String SUMMARY = "Dokumentdistribusjon: %s dokumenter/brev har ikke mottatt kvittering fra utsendingskanal %s";
	private static final String AVVIK_CSV_FILNAVN = "dokumentdistribusjon_avvik-%s-%s.csv";
	private static final String DOKDISTAVSTEMMING_JIRA_BRUKER_NAVN = "srvjiradokdistavstemming";
	private final JiraService jiraService;
	private final JiraCloudClient jiraCloudClient;
	private final JiraAuthProperties jiraAuthProperties;

	public JiraOppgaveService(JiraService jiraService,
							  JiraCloudClient jiraCloudClient,
							  JiraAuthProperties jiraAuthProperties) {
		this.jiraService = jiraService;
		this.jiraCloudClient = jiraCloudClient;
		this.jiraAuthProperties = jiraAuthProperties;
	}

	public JiraSakResponseTo opprettJirasak(DistribusjonKanalCode distribusjonKanal, byte[] csv, int avvikCount, LocalDate avstemmingsfilDato) {
		try {
			Project project = jiraCloudClient.hentJiraCloudProject();

			log.info("Hentet Jira-cloud-prosjekt med key={}, name={}", project.key(), project.name());

			JiraRequest jiraRequest = mapJiraRequest(distribusjonKanal, avvikCount, csv, avstemmingsfilDato);

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

	private JiraRequest mapJiraRequest(DistribusjonKanalCode distribusjonskanal, int avvikCount, byte[] file, LocalDate avstemmingsfilDato) {
		return JiraRequest.builder()
				.summary(format(SUMMARY, avvikCount, distribusjonskanal.name()))
				.description(format(DESCRIPTION, avvikCount, distribusjonskanal.name()))
				.reporter(new JiraRequest.Reporter(jiraAuthProperties.username(), DOKDISTAVSTEMMING_JIRA_BRUKER_NAVN))
				.labels(List.of("dokumentdistribusjon_avvik"))
				.filnavn(format(AVVIK_CSV_FILNAVN, distribusjonskanal.name(), NORSK_LOCAL_DATE_FORMAT.format(avstemmingsfilDato)))
				.vedlegg(file)
				.build();
	}
}
