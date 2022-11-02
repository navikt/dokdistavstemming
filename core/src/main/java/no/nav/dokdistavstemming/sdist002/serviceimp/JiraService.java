package no.nav.dokdistavstemming.sdist002.serviceimp;


import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.jira.JiraConsumer;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import no.nav.dokdistavstemming.domain.to.JiraTransition;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.constants.MDCConstants;
import no.nav.dokdistavstemming.metrics.Monitor;
import no.nav.dokdistavstemming.utils.OppretteJiraSakRequestUtil;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class JiraService {

    private static final String BROWSE = "/browse";
    private static final String TRANSITION_ID = "121";
    private JiraConsumer jiraConsumer;

    public JiraService(JiraConsumer jiraConsumer) {
        this.jiraConsumer = jiraConsumer;
    }

    @Monitor(value = "dokdist_request", extraTags = {"process_code", "oppretteMMAJiraSak"}, percentiles = {0.5, 0.95})
    public JiraSakResponseTo oppretteMMAJiraSak(String distribusjonKanal, File fil, int size) {

        MDC.put(MDCConstants.MDC_REQUEST_ID, "oppretteMMAJiraSak");
        if (!isFilExistOgNotNull(fil)) {
            log.info("Fant ingen avvik fra dokumentdistribusjon(rdist002) og sdist002 kan ikke opprette jira sak");
            return JiraSakResponseTo.builder()
                    .message("Ingen filer og kan ikke opprette jira-sak")
                    .httpStatusCode(HttpStatus.NO_CONTENT.value())
                    .build();
        }

        IssueInput issueInput = OppretteJiraSakRequestUtil.createJiraSaksRequest(jiraConsumer.hentProjekt("MMA"), distribusjonKanal, size);
        validateInput(issueInput);

        try {
            log.info("{} mottat kall til å opprette jira sak", MDC.get(MDCConstants.MDC_REQUEST_ID));
            Issue issue = jiraConsumer.oppretteJiraSak(issueInput);
            jiraConsumer.leggVedlegg(issue.getKey(), fil);
            log.info(String.format("%s har opprettet MMA jira-sak med SaksId=%s SaksKey=%s self=%s",
                    MDC.get(MDCConstants.MDC_REQUEST_ID), issue.getId(), issue.getKey(), issue.getSelf()));
            updateJiraStatus(issue);
            JiraSakResponseTo jiraSakResponseTo = JiraSakResponseTo.builder()
                    .jiraSakKey(issue.getKey())
                    .message(String.format("%s%s/%s", getHostFraUrl(issue.getSelf()), BROWSE, issue.getKey()))
                    .build();

            log.info(String.format("Sdist002 opprettet jira sak med url=%s", jiraSakResponseTo.getMessage()));
            return jiraSakResponseTo;

        } catch (AvstemForsendelseFunctionalException e) {
            log.warn(String.format("%s feilet til å opprette jirasak, En eller flere nødvendige felter i metadata er null eller ugyldig feilmelding=%s", MDC.get(MDCConstants.MDC_REQUEST_ID),
                    e.getMessage()));
            throw new AvstemForsendelseFunctionalException(String.format("%s feilet til å opprette jirasak, En eller flere nødvendige felter i metadata er null eller ugyldig feilmelding=%s", MDC.get(MDCConstants.MDC_REQUEST_ID),
                    e.getMessage()));
        }
    }

    private void updateJiraStatus(Issue issue) {
        Issue updateIssue = jiraConsumer.updateStatus(issue.getKey(), JiraTransition.builder()
                .transition(JiraTransition.Transition.builder().id(TRANSITION_ID).build()).build());
        log.info("Oppdatert sak med key={} til status={}", issue.getKey(), updateIssue.getFields().getStatus().getName());

    }

    private void validateInput(IssueInput issueInput) {
        if (!isGyldigInput(issueInput)) {
            log.error(String.format("En eller flere nødvendige felter mangler eller er null. projectKey=%s, saksTypeNavn=%s",
                    issueInput.getFields().getProject().getKey(), issueInput.getFields().getIssuetype().getName()));
            throw new AvstemForsendelseFunctionalException(String.format("Bestilling kan ikke utføres, nødvendige felter i mangler eller er null. projectKey=%s, saksTypeNavn=%s",
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
                throw new MalformedURLException(String.format("Fant ikke host url med feilmelding=%s", e.getMessage()));
            } catch (MalformedURLException ex) {
                log.error(String.format("Fant ikke host url med feilmelding=%s", ex.getMessage()));
            }
        }
        return hostFraUrl;
    }
}
