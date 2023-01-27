package no.nav.dokdistavstemming.sdist002.serviceimp;


import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.domain.to.JiraTransition;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.metrics.Monitor;
import no.nav.dokdistavstemming.utils.OppretteJiraSakRequestUtil;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;
import static no.nav.dokdistavstemming.constants.MDCConstants.DOK_REQUEST;
import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_REQUEST_ID;

@Component
@Slf4j
public class JiraService {

    private static final String BROWSE = "/browse";
    private static final String TRANSITION_ID = "121";
    private final JiraConsumer jiraConsumer;

    public JiraService(JiraConsumer jiraConsumer) {
        this.jiraConsumer = jiraConsumer;
    }

    @Monitor(value = DOK_REQUEST, extraTags = {"process_code", "oppretteMMAJiraSak"}, percentiles = {0.5, 0.95})
    public JiraSakResponseTo oppretteMMAJiraSak(String distribusjonKanal, File fil, int size) {

        MDC.put(MDC_REQUEST_ID, "oppretteMMAJiraSak");

        if (!isFilExistOgNotNull(fil)) {
            log.info("Fant ingen avvik fra dokumentdistribusjon (rdist002) og sdist002 kan ikke opprette Jira-sak");
            return JiraSakResponseTo.builder()
                    .message("Ingen filer og kan ikke opprette jira-sak")
                    .httpStatusCode(HttpStatus.NO_CONTENT.value())
                    .build();
        }

        IssueInput issueInput = OppretteJiraSakRequestUtil.createJiraSaksRequest(jiraConsumer.hentProjekt("MMA"), distribusjonKanal, size);
        validateInput(issueInput);

        try {
            log.info("{} har mottatt kall om å opprette Jira-sak", MDC.get(MDC_REQUEST_ID));

            Issue issue = jiraConsumer.oppretteJiraSak(issueInput);
            jiraConsumer.leggVedlegg(issue.getKey(), fil);
            log.info("{} har opprettet Jira-sak med SaksId={} SaksKey={} self={}", MDC.get(MDC_REQUEST_ID), issue.getId(), issue.getKey(), issue.getSelf());
            updateJiraStatus(issue);
            JiraSakResponseTo jiraSakResponseTo = JiraSakResponseTo.builder()
                    .jiraSakKey(issue.getKey())
                    .message(format("%s%s/%s", getHostFraUrl(issue.getSelf()), BROWSE, issue.getKey()))
                    .build();

            log.info("Sdist002 har opprettet Jira-sak med url={}", jiraSakResponseTo.getMessage());
            return jiraSakResponseTo;

        } catch (AvstemForsendelseFunctionalException e) {
            log.warn("{} kunne ikke opprette jirasak. Ett eller flere nødvendige felter i metadata er null, eller feil={}",
                    MDC.get(MDC_REQUEST_ID), e.getMessage());
            throw new AvstemForsendelseFunctionalException(format("%s kunne ikke opprette jirasak. Ett eller flere nødvendige felter i metadata er null eller feil=%s", MDC.get(MDC_REQUEST_ID),
                    e.getMessage()));
        }
    }

    private void updateJiraStatus(Issue issue) {
        Issue updateIssue = jiraConsumer.updateStatus(issue.getKey(), JiraTransition.builder()
                .transition(JiraTransition.Transition.builder().id(TRANSITION_ID).build()).build());
        log.info("Har oppdatert Jira-sak med key={} til status={}", issue.getKey(), updateIssue.getFields().getStatus().getName());
    }

    private void validateInput(IssueInput issueInput) {
        if (!isGyldigInput(issueInput)) {
            log.error("Ett eller flere nødvendige felter mangler eller er null. projectKey={}, saksTypeNavn={}",
                    issueInput.getFields().getProject().getKey(), issueInput.getFields().getIssuetype().getName());
            throw new AvstemForsendelseFunctionalException(format("Bestilling kan ikke utføres. Nødvendige felter mangler eller er null. projectKey=%s, saksTypeNavn=%s",
                    issueInput.getFields().getProject().getKey(), issueInput.getFields().getIssuetype().getName()));
        }
    }

    private boolean isGyldigInput(IssueInput issueInput) {
        return !issueInput.getFields().getProject().getKey().isEmpty() && !issueInput.getFields().getIssuetype().getName().isEmpty();
    }

    private boolean isFilExistOgNotNull(File fil) {
        return fil.exists() && fil.length() > 0;
    }

    private String getHostFraUrl(String stringUrl) {
        String hostFraUrl = "";
        try {
            URL url = new URL(stringUrl);
            hostFraUrl = url.getProtocol() + "://" + url.getHost();
        } catch (MalformedURLException e) {
            try {
                throw new MalformedURLException(format("Fant ikke host url med feilmelding=%s", e.getMessage()));
            } catch (MalformedURLException ex) {
                log.error("Fant ikke host url med feilmelding={}", ex.getMessage());
            }
        }
        return hostFraUrl;
    }
}
