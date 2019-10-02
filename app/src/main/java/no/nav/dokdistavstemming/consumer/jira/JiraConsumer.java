package no.nav.dokdistavstemming.consumer.jira;

import com.pep1.jira.client.JIRAClient;
import com.pep1.jira.client.domain.issue.Attachment;
import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.alias.JiraServiceuserAlias;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingTechnicalException;
import no.nav.dokdistavstemming.exceptions.JiraClientException;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.time.Duration;
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class JiraConsumer {

	private static final String ISSUE_CREATE = "/rest/api/2/issue";
	private static final String ATTACHMENTS = "/attachments";


	public final String jiraBaseUri;
	private final String apiBaseUri;
	private final RestTemplate restTemplate;
	private final JiraServiceuserAlias jiraServiceuserAlias;
	private static final Duration DURATION = Duration.ofMillis(30000L);


	public JiraConsumer(@Value("${jira.v1.url}") String jiraBaseUri, RestTemplateBuilder restTemplate, JiraServiceuserAlias jiraServiceuserAlias) {
		this.jiraBaseUri = jiraBaseUri;
		this.restTemplate = restTemplate
				.basicAuthentication(jiraServiceuserAlias.getUsername(), jiraServiceuserAlias.getPassword())
				.setConnectTimeout(DURATION)
				.setReadTimeout(DURATION)
				.build();
		this.apiBaseUri = UriComponentsBuilder.fromUriString(jiraBaseUri).path(ISSUE_CREATE).build().toString();
		this.jiraServiceuserAlias = jiraServiceuserAlias;
	}


	@Retryable(include = DokDistAvstemmingTechnicalException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = "dokdist_consumer_request", extraTags = {"consumer", "JIRA", "process_code", "oppretteJiraSak"}, percentiles = {0.5, 0.95})
	public Issue oppretteJiraSak(@Valid @NotNull IssueInput issueInputRequest) throws JiraClientException {
		try {
			HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);
			ResponseEntity<Issue> responseEntity = restTemplate.exchange(apiBaseUri, HttpMethod.POST, new HttpEntity<>(issueInputRequest, headers), Issue.class);
			return responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			log.warn(String.format("Kall mot jira-sak feilet: %s", e.getMessage()));
			throw new DokDistAvstemmingFunctionalException(
					String.format("Kall mot jira-sak status:%s ,feilet: %s", e.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			log.error(String.format("En feil oppsto. Bestilling kan ikke utføres",e.getMessage()));
			throw new DokDistAvstemmingTechnicalException(
					String.format("Kall mot jira-sak  feilet teknisk. statusKode=%s feilmelding=%s ", e.getStatusCode(), e.getMessage()), e);
		}
	}

	@Retryable(include = DokDistAvstemmingTechnicalException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = "dokdist_consumer_request", extraTags = {"consumer", "JIRA", "process_code", "laggeVedlagg"}, percentiles = {0.5, 0.95})
	public String laggeVedlagg(@NonNull String key, @NonNull File file) throws JiraClientException {
		if (key == null) {
			throw new IllegalArgumentException("Nøkklen er market @NonNull men det er null");
		} else if (file == null) {
			throw new IllegalArgumentException("ressurser er market @NonNull men det er null");
		}
		try {
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap();
			map.add("file", new FileSystemResource(file));
			HttpHeaders headers = createSecurityHeaders(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity(map, headers);
			return this.restTemplate.exchange(apiBaseUri + String.format("/%s%s", key, ATTACHMENTS), HttpMethod.POST, requestEntity, String.class).getBody();
		} catch (JiraClientException e) {
			log.error(String.format("En feil oppsto. Bestilling kan ikke utføres",e.getMessage()));
			throw new JiraClientException(e.getStatusCode(),String.format("En feil oppsto. Bestilling kan ikke utføres",e.getMessage()));
		}
	}


	public HttpHeaders createSecurityHeaders(MediaType mediaType) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(jiraServiceuserAlias.getUsername(), jiraServiceuserAlias.getPassword());
		headers.add("X-Atlassian-Token", "no-check");
		headers.setContentType(mediaType);
		return headers;
	}


}
