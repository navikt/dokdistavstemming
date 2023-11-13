package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.consumer.jira.domain.Component;
import no.nav.dokdistavstemming.consumer.jira.domain.Issue;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueFields;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueInput;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueType;
import no.nav.dokdistavstemming.consumer.jira.domain.Project;
import no.nav.dokdistavstemming.consumer.jira.domain.Reporter;
import no.nav.dokdistavstemming.consumer.jira.domain.Status;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.domain.to.JiraTransition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.io.File;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JiraServiceTest {

	private static final String JIRA_SAK_URL = "https://jira-q1.adeo.no/browse/MMA-134";
	private static final String ATTACHMENT_URL = "https://jira-q1.adeo.no/rest/api/2/issue/534999/attachments";
	private static final String PROJECT_KEY = "MMA";

	@Mock
	private JiraConsumer jiraConsumer;

	@InjectMocks
	private JiraService jiraService;

	@Test
	public void shoudOpprettetJiraSakwithVedlegg() throws Exception {
		when(jiraConsumer.opprettJiraSak(any(IssueInput.class))).thenReturn(createIssue());
		when(jiraConsumer.hentProsjekt(any(String.class))).thenReturn(createProject());
		when(jiraConsumer.oppdaterStatus(anyString(), any(JiraTransition.class))).thenReturn(updateIssue());
		File avvikFil = new File(new ClassPathResource("__files/csv/csvfil_print.csv").getFile().toString());
		when(jiraConsumer.leggTilVedlegg("MMA-134", avvikFil)).thenReturn(ATTACHMENT_URL);

		JiraSakResponseTo jiraSakResponseTo = jiraService.opprettJirasak(DistribusjonKanalCode.PRINT.name(), avvikFil, 10);

		verify(jiraConsumer, times(1)).opprettJiraSak(any(IssueInput.class));
		verify(jiraConsumer, times(1)).hentProsjekt(anyString());
		assertThat(jiraSakResponseTo.getMessage(), is(JIRA_SAK_URL));
	}

	@Test
	public void shouldUpdateStatusToKlarForArbeid() throws Exception {
		when(jiraConsumer.opprettJiraSak(any(IssueInput.class))).thenReturn(createIssue());
		when(jiraConsumer.hentProsjekt(any(String.class))).thenReturn(createProject());
		when(jiraConsumer.oppdaterStatus(anyString(), any(JiraTransition.class))).thenReturn(updateIssue());
		File avvikFil = new File(new ClassPathResource("__files/csv/dokdist1.csv").getFile().toString());
		when(jiraConsumer.leggTilVedlegg("MMA-134", avvikFil)).thenReturn(ATTACHMENT_URL);

		JiraSakResponseTo jiraSakResponseTo = jiraService.opprettJirasak(DistribusjonKanalCode.PRINT.name(), avvikFil, 10);

		verify(jiraConsumer, times(1)).opprettJiraSak(any(IssueInput.class));
		verify(jiraConsumer, times(1)).hentProsjekt(anyString());
		assertThat(jiraSakResponseTo.getMessage(), is(JIRA_SAK_URL));
	}

	@Test
	public void opprettJiraSakThrowsExceptionIfAvstemmingFrosendelseErUtenVedlegg() {
		File avvikFil = new File("");
		JiraSakResponseTo jiraSakResponseTo = jiraService.opprettJirasak(DistribusjonKanalCode.PRINT.name(), avvikFil, 0);
		assertThat(jiraSakResponseTo.getMessage(), is("Kan ikke opprette Jira-sak. Fant ingen csv-fil"));
		assertThat(jiraSakResponseTo.getHttpStatusCode(), is(HttpStatus.NO_CONTENT.value()));
	}

	private Project createProject() {
		Component component = new Component("https://jira-q1.adeo.no/rest/api/2/component/26154", "26154", "Dokumentdistribusjon",
				false);

		IssueType issueType = new IssueType("https://jira.adeo.no/rest/api/2/issuetype/10901", "10901", "En oppgave som må utføres.", "Oppgave", false);

		return new Project("Project", "https://jira.adeo.no/rest/api/2/project/19377", "19954", "MMA",
				null, "Team Dokumentløsninger", null, singletonList(component), singletonList(issueType), null);
	}

	private Issue createIssue() {

		Component component = Component.builder()
				.id("26154")
				.name("Dokumentdistribusjon")
				.self("https://jira-q1.adeo.no/rest/api/2/component/26154")
				.build();
		Reporter reporter = Reporter.builder()
				.self("https://jira-q1.adeo.no/rest/api/2/user?username=srvjiradokdistavstemming")
				.name("srvjiradokdistavstemming")
				.key("srvjiradokdistavstemming")
				.build();

		Status status = new Status("https://jira-q1.adeo.no/rest/api/2/status/26154", null,
				"Klar for arbeid", "26154", null);

		Project project = Project.builder().self("https://jira-q1.adeo.no/rest/api/2/issue/534999")
				.id("MMA-134")
				.key("MMA")
				.components(singletonList(component))
				.build();

		IssueFields issueFields = IssueFields.builder()
				.project(project)
				.status(status)
				.reporter(reporter)
				.build();

		return new Issue(null, "534999", "https://jira-q1.adeo.no/rest/api/2/issue/534999",
				"MMA-134", issueFields, status);
	}

	private Issue updateIssue() {

		Component component = Component.builder()
				.id("26154")
				.name("Dokumentdistribusjon")
				.self("https://jira-q1.adeo.no/rest/api/2/component/26154")
				.build();

		Reporter reporter = Reporter.builder()
				.self("https://jira-q1.adeo.no/rest/api/2/user?username=srvjiradokdistavstemming")
				.name("srvjiradokdistavstemming")
				.key("srvjiradokdistavstemming")
				.build();


		Status status = new Status("https://jira-q1.adeo.no/rest/api/2/status/26154", null, "Klar for arbeid", "26154", null);

		Project project = Project.builder()
				.key(PROJECT_KEY)
				.components(singletonList(component))
				.build();

		IssueFields issueFields = IssueFields.builder()
				.project(project)
				.summary("")
				.status(status)
				.reporter(reporter)
				.build();

		return new Issue(null, "534999", "https://jira-q1.adeo.no/rest/api/2/issue/534999",
				"MMA-134", issueFields, status);
	}

}
