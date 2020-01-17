package no.nav.dokdistavstemming.service;


import com.pep1.jira.client.domain.issue.Component;
import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.IssueType;
import com.pep1.jira.client.domain.issue.Reporter;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.alias.JiraServiceuserAlias;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.service.serviceimp.AvstemForsendelseService;
import no.nav.dokdistavstemming.service.serviceimp.CSVProdusereImpl;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JiraServiceTest {


	private static final String JIRA_SAK_URL = "https://jira-q1.adeo.no/browse/MMA-134";

	@Mock
	private JiraConsumer jiraConsumer;

	@Inject
	private CSVProdusere csvProdusereImpl;

	@Mock
	private File fil;

	@Mock
	private AvstemForsendelseService avstemForsendelseService;
	@Mock
	private MeterRegistry meterRegistry;
	private JiraServiceuserAlias jiraServiceuserAlias;
	@Mock
	private Counter counterMock;

	@InjectMocks
	private JiraService jiraService;

	@BeforeEach
	public void setUp() {
		csvProdusereImpl = mock(CSVProdusereImpl.class);
		fil = mock(File.class);
		jiraServiceuserAlias = new JiraServiceuserAlias("test", "test");
		jiraService = new JiraService(jiraConsumer, meterRegistry);

	}

	@Test
	public void shoudOpprettetJiraSakwithVedlegg() throws Exception {
		when(jiraConsumer.oppretteJiraSak(any(IssueInput.class))).thenReturn(createIssue());
		when(jiraConsumer.hentProjekt(any(String.class))).thenReturn(createProject());
		File avvikFil = new File(new ClassPathResource("__files/csvfil_print.csv").getFile().toString());
		when(jiraConsumer.leggVedlegg("MMA-134",avvikFil)).thenReturn("https://jira-q1.adeo.no/rest/api/2/issue/534999/attachments");
		when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);

		JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak(DistribusjonKanalCode.PRINT.name(), avvikFil);

		verify(meterRegistry, times(1)).counter(anyString(), anyString(), anyString());
		verify(jiraConsumer, times(1)).oppretteJiraSak(any(IssueInput.class));
		verify(jiraConsumer, times(1)).hentProjekt(anyString());
		assertThat(jiraSakResponseTo.getMessage(), is(JIRA_SAK_URL));
	}

	@Test
	public void opprettJiraSakThrowsExceptionIfAvstemmingFrosendelseErUtenVedlegg() throws Exception {
		File avvikFil = new File("");
		JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak(DistribusjonKanalCode.PRINT.name(), avvikFil);
		assertThat(jiraSakResponseTo.getMessage(), is("Ingen filer og kan ikke opprette jira-sak"));
		assertThat(jiraSakResponseTo.getHttpStatusCode(), is(HttpStatus.NO_CONTENT.value()));
	}


	private Project createProject() {
		Project project = new Project();
		project.setExpand("Project");
		Component component = new Component();
		component.setSelf("https://jira-q1.adeo.no/rest/api/2/component/26154");
		component.setId("26154");
		component.setName("Dokumentdistribusjon");
		component.setIsAssigneeTypeValid(false);

		IssueType issueType = new IssueType();
		issueType.setName("Oppgave");
		issueType.setSelf("https://jira.adeo.no/rest/api/2/issuetype/10901");
		issueType.setId("10901");
		issueType.setDescription("En oppgave som må utføres.");

		project.setSelf("https://jira.adeo.no/rest/api/2/project/19377");
		project.setId("19954");
		project.setKey("MMA");
		project.setName("Team Dokumentløsninger");
		project.setIssueTypes(Arrays.asList(issueType));
		project.setComponents(Arrays.asList(component));
		return project;
	}


	private final Issue createIssue() {

		Issue issue = new Issue();
		issue.setSelf("https://jira-q1.adeo.no/rest/api/2/issue/534999");
		Project project = new Project();
		project.setKey("MMA");
		issue.setKey("MMA-134");
		issue.setId("534999");
		IssueFields issueFields = new IssueFields();
		Component component = new Component();
		component.setSelf("https://jira-q1.adeo.no/rest/api/2/component/26154");
		component.setId("26154");
		component.setName("Dokumentdistribusjon");
		Reporter reporter = new Reporter();
		reporter.setName("srvjiradokdistavstemming");
		reporter.setKey("srvjiradokdistavstemming");
		reporter.setSelf("https://jira-q1.adeo.no/rest/api/2/user?username=srvjiradokdistavstemming");
		issueFields.setComponents(Arrays.asList(component));
		issueFields.setProject(project);
		issue.setFields(issueFields);

		return issue;

	}

}
