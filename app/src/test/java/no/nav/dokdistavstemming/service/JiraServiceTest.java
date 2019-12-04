package no.nav.dokdistavstemming.service;


import com.pep1.jira.client.domain.issue.Component;
import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.Reporter;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.alias.JiraServiceuserAlias;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.service.serviceimp.AvstemForsendelseService;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.dokdistavstemming.utils.TestUtils.classpathToString;
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

	@Mock
	private JiraConsumer jiraConsumer;

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
		jiraServiceuserAlias = new JiraServiceuserAlias("test", "test");
		jiraService = new JiraService(jiraConsumer, avstemForsendelseService, meterRegistry);

	}

	@Test
	public void shoudOpprettetJiraSakwithVedlegg() throws Exception {
		when(jiraConsumer.oppretteJiraSak(any(IssueInput.class))).thenReturn(createIssue());
		when(jiraConsumer.hentProjekt(any(String.class))).thenReturn(createProject());
		File avvikFiler = new File(classpathToString("__files/csvfil_print.csv"));
		when(avstemForsendelseService.henteDokDistFil()).thenReturn(Arrays.asList(avvikFiler));
		when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counterMock);


		JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak();

		verify(meterRegistry, times(1)).counter(anyString(), anyString(), anyString());
		verify(jiraConsumer, times(1)).oppretteJiraSak(any(IssueInput.class));
		verify(jiraConsumer,times(1)).hentProjekt(anyString());
		verify(avstemForsendelseService, times(1)).henteDokDistFil();
		assertThat(jiraSakResponseTo.getMessage(), is(JIRA_SAK_URL));
	}

	@Test
	public void opprettJiraSakThrowsExceptionIfAvstemmingFrosendelseErUtenVedlegg() throws Exception {
		when(avstemForsendelseService.henteDokDistFil()).thenReturn(Collections.emptyList());
		JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak();
		verify(avstemForsendelseService, times(1)).henteDokDistFil();
		assertThat(jiraSakResponseTo.getMessage(), is("Ingen filer og kan ikke opprette jira-sak"));
		assertThat(jiraSakResponseTo.getHttpStatusCode(), is(HttpStatus.NO_CONTENT.value()));
	}


	private Project createProject() {
		Project project = new Project();
		Component component = new Component();
		component.setSelf("https://jira-q1.adeo.no/rest/api/2/component/26154");
		component.setId("26154");
		component.setName("Dokumentdistribusjon");
		component.setIsAssigneeTypeValid(false);
		project.setId("19377");
		project.setKey("MMA");
		project.setName("Test Test");
		project.setComponents(Arrays.asList(component));
		return project;
	}


	private final Issue createIssue() {

		Issue issue = new Issue();
		issue.setSelf("https://jira-q1.adeo.no/rest/api/2/issue/534999");
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
		issue.setFields(issueFields);

		return issue;

	}

}
