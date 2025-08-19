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

import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.PRINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
		when(jiraService.opprettJiraMMAOppgaveMedVedlegg(any(JiraRequest.class))).thenReturn(JiraResponse.builder().jiraIssueKey(MMA_OPPGAVE_ID).build());
		byte[] avvikFil = new ClassPathResource("__files/csv/csvfil_print.csv").getContentAsByteArray();

		JiraSakResponseTo jiraSakResponseTo = jiraOppgaveService.opprettJirasak(PRINT.name(), avvikFil, 10);

		assertThat(jiraSakResponseTo.getJiraSakKey()).isEqualTo(MMA_OPPGAVE_ID);
	}

	@Test
	public void shouldUpdateStatusToKlarForArbeid() throws Exception {
		when(jiraService.opprettJiraMMAOppgaveMedVedlegg(any(JiraRequest.class))).thenReturn(JiraResponse.builder().jiraIssueKey(MMA_OPPGAVE_ID)
				.message(JIRA_SAK_URL).build());

		byte[] avvikFil = new ClassPathResource("__files/csv/dokdist1.csv").getContentAsByteArray();

		JiraSakResponseTo jiraSakResponseTo = jiraOppgaveService.opprettJirasak(PRINT.name(), avvikFil, 10);

		assertThat(jiraSakResponseTo.getMessage()).isEqualTo(JIRA_SAK_URL);
	}
}
