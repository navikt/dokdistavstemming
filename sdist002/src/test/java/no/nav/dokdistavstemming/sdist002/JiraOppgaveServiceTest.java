package no.nav.dokdistavstemming.sdist002;

import no.nav.dok.jiraapi.JiraRequest;
import no.nav.dok.jiraapi.JiraResponse;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.PRINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@ExtendWith(MockitoExtension.class)
class JiraOppgaveServiceTest {

	private static final String JIRA_SAK_URL = "https://jira-q1.adeo.no/browse/MMA-134";
	private static final String MMA_OPPGAVE_ID = "MMA-134";

	@Mock
	private JiraService jiraService;


	@InjectMocks
	private JiraOppgaveService jiraOppgaveService;

	@Test
	public void shoudOpprettetJiraSakwithVedlegg() throws Exception {
		when(jiraService.opprettJiraOppgaveVedVedlegg(any(JiraRequest.class))).thenReturn(JiraResponse.builder().jiraIssueKey(MMA_OPPGAVE_ID).build());
		File avvikFil = new File(new ClassPathResource("__files/csv/csvfil_print.csv").getFile().toString());

		JiraSakResponseTo jiraSakResponseTo = jiraOppgaveService.opprettJirasak(PRINT.name(), avvikFil, 10);

		assertThat(jiraSakResponseTo.getJiraSakKey()).isEqualTo(MMA_OPPGAVE_ID);
	}

	@Test
	public void shouldUpdateStatusToKlarForArbeid() throws Exception {
		when(jiraService.opprettJiraOppgaveVedVedlegg(any(JiraRequest.class))).thenReturn(JiraResponse.builder().jiraIssueKey(MMA_OPPGAVE_ID)
				.message(JIRA_SAK_URL).build());

		File avvikFil = new File(new ClassPathResource("__files/csv/dokdist1.csv").getFile().toString());

		JiraSakResponseTo jiraSakResponseTo = jiraOppgaveService.opprettJirasak(PRINT.name(), avvikFil, 10);

		assertThat(jiraSakResponseTo.getMessage()).isEqualTo(JIRA_SAK_URL);
	}

	@Test
	public void opprettJiraSakThrowsExceptionIfAvstemmingFrosendelseErUtenVedlegg() {
		File avvikFil = new File("");

		JiraSakResponseTo jiraSakResponseTo = jiraOppgaveService.opprettJirasak(PRINT.name(), avvikFil, 0);

		assertThat(jiraSakResponseTo.getMessage()).isEqualTo("Kan ikke opprette Jira-sak. Fant ingen csv-fil");
		assertThat(jiraSakResponseTo.getHttpStatusCode()).isEqualTo(NO_CONTENT.value());
	}

}
