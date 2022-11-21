package no.nav.dokdistavstemming.consumer.jira;

import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;
import com.pep1.jira.client.error.JIRAClientException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.domain.to.JiraTransition;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseTechnicalException;
import no.nav.dokdistavstemming.exceptions.JiraClientException;
import no.nav.dokdistavstemming.metrics.Monitor;
import no.nav.dokdistavstemming.utils.CallIdInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.time.Duration;

import static no.nav.dokdistavstemming.constants.MDCConstants.DOK_REQUEST;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class JiraConsumer {

	private static final String ISSUE_CREATE = "/rest/api/2/issue";
	private static final String PROJECT_HENT = "/rest/api/2/project";
	private static final String ATTACHMENTS = "/attachments";
	private static final Duration DURATION = Duration.ofMillis(30000L);
	private final String jiraBaseUri;
	private final String apiBaseUri;
	private final RestTemplate restTemplate;
	private final DokdistavstemmingProperties dokdistavstemmingProperties;

	public JiraConsumer(RestTemplateBuilder restTemplateBuilder, DokdistavstemmingProperties dokdistavstemmingProperties) {
		this.jiraBaseUri = dokdistavstemmingProperties.getJira().getUrl();
		this.restTemplate = restTemplateBuilder
				.interceptors(new CallIdInterceptor())
				.setReadTimeout(DURATION)
				.setConnectTimeout(DURATION)
				.basicAuthentication(dokdistavstemmingProperties.getJira().getUsername(), dokdistavstemmingProperties.getJira().getPassword())
				.build();
		this.apiBaseUri = UriComponentsBuilder.fromUriString(jiraBaseUri).path(ISSUE_CREATE).build().toString();
		this.dokdistavstemmingProperties = dokdistavstemmingProperties;
	}

	@Retryable(include = AvstemForsendelseTechnicalException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "JIRA", "process_code", "oppretteJiraSak"}, percentiles = {0.5, 0.95})
	public Issue oppretteJiraSak(@Valid @NotNull IssueInput issueInputRequest) {
		try {
			HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);
			ResponseEntity<Issue> responseEntity = restTemplate.exchange(apiBaseUri, HttpMethod.POST,
					new HttpEntity<>(issueInputRequest, headers), Issue.class);
			return responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			log.warn(String.format("Kall mot jira feilet med url=%s, feilmelding: %s", apiBaseUri, e.getMessage()));
			throw new AvstemForsendelseFunctionalException(
					String.format("Kall mot jira feilet med url=%s, status:%s ,feilmelding: %s", apiBaseUri, e.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			log.error(String.format("En feil oppsto. Bestilling kan ikke utføres feilmelding=%s", e.getMessage()));
			throw new AvstemForsendelseTechnicalException(
					String.format("Kall mot jira-sak  feilet teknisk. statusKode=%s feilmelding=%s ", e.getStatusCode(), e.getMessage()), e);
		}
	}

	@Retryable(include = AvstemForsendelseTechnicalException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "JIRA", "process_code", "leggVedlegg"}, percentiles = {0.5, 0.95})
	public String leggVedlegg(String key, @NonNull File file) {
		if (key == null) {
			throw new IllegalArgumentException("MMA Key er null og kan ikke legge fil til jira saken");
		} else if (file.length() == 0 && !file.exists()) {
			throw new IllegalArgumentException("ressurser er null og kan ikke opprette jira sak");
		}
		try {
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap();
			map.add("file", new FileSystemResource(file));
			HttpHeaders headers = createSecurityHeaders(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity(map, headers);
			return this.restTemplate.exchange(apiBaseUri + String.format("/%s%s", key, ATTACHMENTS), HttpMethod.POST, requestEntity, String.class).getBody();

		} catch (JiraClientException e) {
			log.error(String.format("En feil oppstod. Bestilling kan ikke utføres, MMA-Key=%s,filNavn=%s, feilmelding=%s", key,
					file.getName(), e.getMessage()));
			throw new JiraClientException(e.getStatus(), e.getErrorMessage());
		}
	}

	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "JIRA", "process_code", "hentProjekt"}, percentiles = {0.5, 0.95})
	public Project hentProjekt(@Valid @RequestParam(value = "key") String projectKey) {

		if (projectKey == null) {
			throw new AvstemForsendelseFunctionalException(String.format("Fant ikke projekt key med projectKey=%s", projectKey));
		}

		HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);
		try {
			ResponseEntity<Project> responseEntity = restTemplate.exchange(jiraBaseUri + String.format("%s/%s", PROJECT_HENT, projectKey),
					HttpMethod.GET, new HttpEntity<>(headers), Project.class);

			return responseEntity.getBody();
		} catch (JiraClientException e) {
			throw new JiraClientException(String.format("Feil, fant ikke project med projectKey=%s, feilmelding=%s", projectKey, e.getMessage()));
		}
	}

	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "JIRA", "process_code", "getIssue"}, percentiles = {0.5, 0.95})
	public Issue getIssue(final String sakKey) {
		HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);
		try {
			ResponseEntity<Issue> responseEntity = restTemplate.exchange(apiBaseUri + String.format("/%s", sakKey),
					HttpMethod.GET, new HttpEntity<>(headers), Issue.class);
			return responseEntity.getBody();
		} catch (JiraClientException e) {
			throw new JiraClientException(String.format("Feil, fant ikke saken med sakKey=%s, feilmelding=%s", sakKey, e.getMessage()));
		}
	}

	@Retryable(include = AvstemForsendelseTechnicalException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = DOK_REQUEST, extraTags = {"consumer", "JIRA", "process_code", "updateStatus"}, percentiles = {0.5, 0.95})
	public Issue updateStatus(final String key, @Valid @NonNull JiraTransition transition) throws JIRAClientException {
		HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);

		try {
			restTemplate.exchange(apiBaseUri + "/" + key + "/transitions", HttpMethod.POST,
					new HttpEntity<>(transition, headers), String.class);
			return getIssue(key);

		} catch (JiraClientException e) {
			throw new JiraClientException(String.format("Feil, fant ikke meta fields med feilmelding=%s", e.getMessage()));
		}
	}

	private HttpHeaders createSecurityHeaders(MediaType mediaType) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(dokdistavstemmingProperties.getJira().getUsername(), dokdistavstemmingProperties.getJira().getPassword());
		headers.add("X-Atlassian-Token", "no-check");
		headers.setContentType(mediaType);
		return headers;
	}

}
