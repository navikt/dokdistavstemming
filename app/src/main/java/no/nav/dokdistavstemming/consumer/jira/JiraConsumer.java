package no.nav.dokdistavstemming.consumer.jira;

import com.pep1.jira.client.domain.issue.Attachment;
import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import lombok.NonNull;
import no.nav.dokdistavstemming.consumer.sts.STSResponse;
import no.nav.dokdistavstemming.consumer.sts.STSRestConsumer;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingTechnicalException;
import no.nav.dokdistavstemming.exceptions.JiraClientException;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
public class JiraConsumer {

	private static final String ISSUE_CREATE = "/rest/api/2/issue";
	private static final String ATTACHMENTS = "/attachments";


	private final String jiraBaseUri;
	private final String apiBaseUri;
	private final RestTemplate restTemplate;
	private final STSRestConsumer stsRestConsumer;


	public JiraConsumer(@Value("${jira.host.url}") String jiraBaseUri, RestTemplate restTemplate, STSRestConsumer stsRestConsumer) {
		this.jiraBaseUri = jiraBaseUri;
		this.restTemplate = restTemplate;
		this.stsRestConsumer = stsRestConsumer;
		this.apiBaseUri = UriComponentsBuilder.fromUriString(jiraBaseUri).path(ISSUE_CREATE).build().toString();
	}


	@Retryable(include = DokDistAvstemmingTechnicalException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = "dokdist_consumer_request", extraTags = {"consumer", "JIRA", "process_code", "oppretteJiraSak"}, percentiles = {0.5, 0.95})
	public Issue oppretteJiraSak(@Valid @NotNull IssueInput issueInputRequest) throws JiraClientException {
		try {
			HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);
			ResponseEntity<Issue> responseEntity = restTemplate.exchange(apiBaseUri, HttpMethod.POST, new HttpEntity<>(issueInputRequest, headers), Issue.class);
			return responseEntity.getBody();
		} catch (HttpClientErrorException e) {
			throw new DokDistAvstemmingFunctionalException(
					String.format("Kall mot jira-sak feilet: %s", e.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new DokDistAvstemmingTechnicalException(
					String.format("Kall mot jira-sak  feilet teknisk. statusKode=%s feilmelding=%s ", e.getStatusCode(), e.getMessage()), e);
		}
	}

	@Retryable(include = DokDistAvstemmingTechnicalException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
	@Monitor(value = "dokdist_consumer_request", extraTags = {"consumer", "JIRA", "process_code", "laggeVedlagg"}, percentiles = {0.5, 0.95})
	public List<Attachment> laggeVedlagg(@NonNull String key, @NonNull Resource resource) throws JiraClientException {
		if (key == null) {
			throw new IllegalArgumentException("Nøkklen er market @NonNull men det er null");
		} else if (resource == null) {
			throw new IllegalArgumentException("ressurser er market @NonNull men det er null");
		}
		try {
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap();
			map.add("file", resource);
			HttpHeaders headers = createSecurityHeaders(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity(map, headers);
			return (List) this.restTemplate.exchange(apiBaseUri + String.format("/%s%s", key, ATTACHMENTS), HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<Attachment>>() {
			}).getBody();
		} catch (JiraClientException e) {
			throw new JiraClientException(e.getStatusCode(), e.getMessage());
		}
	}


	protected HttpHeaders createSecurityHeaders(MediaType mediaType) {
		STSResponse response = stsRestConsumer.getServiceuserOIDCToken().getBody();
		String oidcBearerToken = "Bearer " + response.getAccessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(mediaType);
		headers.add(HttpHeaders.AUTHORIZATION, oidcBearerToken);
		return headers;
	}

}
