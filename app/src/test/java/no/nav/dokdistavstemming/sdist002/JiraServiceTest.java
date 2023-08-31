package no.nav.dokdistavstemming.sdist002;


import com.pep1.jira.client.domain.issue.Component;
import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.IssueType;
import com.pep1.jira.client.domain.issue.Reporter;
import com.pep1.jira.client.domain.issue.Status;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.domain.to.JiraTransition;
import no.nav.dokdistavstemming.sdist002.serviceimp.JiraService;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private JiraConsumer jiraConsumer;

    @InjectMocks
    private JiraService jiraService;

    @BeforeEach
    public void setUp() {
        jiraService = new JiraService(jiraConsumer);
    }

    @Test
    public void shoudOpprettetJiraSakwithVedlegg() throws Exception {
        when(jiraConsumer.opprettJiraSak(any(IssueInput.class))).thenReturn(createIssue());
        when(jiraConsumer.hentProsjekt(any(String.class))).thenReturn(createProject());
        when(jiraConsumer.oppdaterStatus(anyString(), any(JiraTransition.class))).thenReturn(updateIssue());
        File avvikFil = new File(new ClassPathResource("__files/csv/csvfil_print.csv").getFile().toString());
        when(jiraConsumer.leggTilVedlegg("MMA-134", avvikFil)).thenReturn(ATTACHMENT_URL);

        JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak(DistribusjonKanalCode.PRINT.name(), avvikFil, 10);

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

        JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak(DistribusjonKanalCode.PRINT.name(), avvikFil, 10);

        verify(jiraConsumer, times(1)).opprettJiraSak(any(IssueInput.class));
        verify(jiraConsumer, times(1)).hentProsjekt(anyString());
        assertThat(jiraSakResponseTo.getMessage(), is(JIRA_SAK_URL));
    }

    @Test
    public void opprettJiraSakThrowsExceptionIfAvstemmingFrosendelseErUtenVedlegg() {
        File avvikFil = new File("");
        JiraSakResponseTo jiraSakResponseTo = jiraService.oppretteMMAJiraSak(DistribusjonKanalCode.PRINT.name(), avvikFil, 0);
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
        project.setIssueTypes(singletonList(issueType));
        project.setComponents(singletonList(component));
        return project;
    }


    private Issue createIssue() {

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
        Status status = new Status();
        status.setId("26154");
        status.setSelf("https://jira-q1.adeo.no/rest/api/2/status/26154");
        status.setName("Klar for arbeid");
        issueFields.setStatus(status);
        issueFields.setComponents(singletonList(component));
        issueFields.setProject(project);
        issue.setFields(issueFields);

        return issue;

    }


    private Issue updateIssue() {

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
        issueFields.setComponents(singletonList(component));
        issueFields.setProject(project);
        Status status = new Status();
        status.setId("26154");
        status.setSelf("https://jira-q1.adeo.no/rest/api/2/status/26154");
        status.setName("Klar for arbeid");
        issueFields.setStatus(status);
        issue.setFields(issueFields);

        return issue;
    }

}
