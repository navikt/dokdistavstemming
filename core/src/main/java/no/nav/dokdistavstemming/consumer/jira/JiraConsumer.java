package no.nav.dokdistavstemming.consumer.jira;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.consumer.jira.domain.Issue;
import no.nav.dokdistavstemming.consumer.jira.domain.IssueInput;
import no.nav.dokdistavstemming.consumer.jira.domain.Project;
import no.nav.dokdistavstemming.domain.to.JiraTransition;
import no.nav.dokdistavstemming.exceptions.JiraFunctionalException;
import no.nav.dokdistavstemming.exceptions.JiraTechnicalException;
import no.nav.dokdistavstemming.utils.CallIdInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.time.Duration;

import static java.lang.String.format;

@Slf4j
@Component
public class JiraConsumer {

	private static final String ISSUE = "/rest/api/2/issue";
	private static final String PROJECT = "/rest/api/2/project";
	private static final String ATTACHMENTS = "/attachments";
	private static final String TRANSITIONS = "/transitions";
	private static final Duration DURATION = Duration.ofMillis(30000L);
	private final String issueBaseUri;
	private final String projectBaseUri;
	private final RestTemplate restTemplate;

	public JiraConsumer(RestTemplateBuilder restTemplateBuilder,
						DokdistavstemmingProperties dokdistavstemmingProperties) {
		String jiraBaseUri = dokdistavstemmingProperties.getJira().getUrl();
		this.issueBaseUri = UriComponentsBuilder.fromUriString(jiraBaseUri).path(ISSUE).build().toString();
		this.projectBaseUri = UriComponentsBuilder.fromUriString(jiraBaseUri).path(PROJECT).build().toString();
		this.restTemplate = restTemplateBuilder
				.interceptors(new CallIdInterceptor())
				.setReadTimeout(DURATION)
				.setConnectTimeout(DURATION)
				.basicAuthentication(dokdistavstemmingProperties.getJira().getUsername(), dokdistavstemmingProperties.getJira().getPassword())
				.build();
	}

	@Retryable(include = JiraTechnicalException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
	public Issue opprettJiraSak(@Valid @NotNull IssueInput issueInputRequest) {
		HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);

		try {
			return restTemplate.exchange(issueBaseUri, HttpMethod.POST, new HttpEntity<>(issueInputRequest, headers), Issue.class).getBody();
		} catch (HttpClientErrorException e) {
			log.warn("oppretteJiraSak feilet funksjonelt med url={}, status={} feilmelding={}", issueBaseUri, e.getStatusCode(), e.getMessage());
			throw new JiraFunctionalException(
					format("Kall mot jira feilet funksjonelt med url=%s, status=%s, feilmelding=%s", issueBaseUri, e.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			log.error("oppretteJiraSak feilet teknisk med feilmelding={}", e.getMessage());
			throw new JiraTechnicalException(
					format("Kall mot Jira feilet teknisk med statusKode=%s, feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	@Retryable(include = JiraTechnicalException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
	public String leggTilVedlegg(String key, @NotNull File file) {
		if (key == null) {
			throw new IllegalArgumentException("Kan ikke legge til vedlegg på Jira-saken. Prosjekt-key er null.");
		} else if (file.length() == 0 && !file.exists()) {
			throw new IllegalArgumentException("Kan ikke legge til vedlegg på Jira-saken. CSV-fil er null.");
		}

		String url = format("%s/%s%s", issueBaseUri, key, ATTACHMENTS);
		HttpHeaders headers = createSecurityHeaders(MediaType.MULTIPART_FORM_DATA);

		try {
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("file", new FileSystemResource(file));
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

			return this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();

		} catch (HttpClientErrorException e) {
			log.warn("leggTilVedlegg ({}) feilet funksjonelt med url={}, status={}, feilmelding={}", file.getName(), url, e.getStatusCode(), e.getMessage());
			throw new JiraFunctionalException(
					format("Kall mot jira feilet funksjonelt med url=%s, status=%s, feilmelding=%s", url, e.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			log.error("leggTilVedlegg ({}) feilet teknisk med feilmelding={}", file.getName(), e.getMessage());
			throw new JiraTechnicalException(
					format("Kall mot Jira feilet teknisk med statusKode=%s, feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	@Retryable(include = JiraTechnicalException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
	public Project hentProsjekt(String projectKey) {

		if (projectKey == null) {
			throw new IllegalArgumentException("Kan ikke hente Jira-prosjekt. Project-key er null");
		}

		HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);
		String url = format("%s/%s", projectBaseUri, projectKey);

		try {
			return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Project.class).getBody();
		} catch (HttpClientErrorException e) {
			log.warn("hentProsjekt feilet funksjonelt med url={}, status={}, feilmelding={}", url, e.getStatusCode(), e.getMessage());
			throw new JiraFunctionalException(
					format("Kall mot jira feilet funksjonelt med url=%s, status=%s, feilmelding=%s", issueBaseUri, e.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			log.error("hentProsjekt feilet teknisk med feilmelding={}", e.getMessage());
			throw new JiraTechnicalException(
					format("Kall mot Jira feilet teknisk med statusKode=%s, feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	@Retryable(include = JiraTechnicalException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
	public Issue hentIssue(final String sakKey) {
		HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);
		String url = format("%s/%s", issueBaseUri, sakKey);

		try {
			return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Issue.class).getBody();
		} catch (HttpClientErrorException e) {
			log.warn("hentIssue feilet funksjonelt med url={}, status={}, feilmelding={}", url, e.getStatusCode(), e.getMessage());
			throw new JiraFunctionalException(
					format("Kall mot jira feilet funksjonelt med url=%s, status=%s, feilmelding=%s", url, e.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			log.error("hentIssue feilet teknisk med feilmelding={}", e.getMessage());
			throw new JiraTechnicalException(
					format("Kall mot Jira feilet teknisk med statusKode=%s, feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	@Retryable(include = JiraTechnicalException.class, backoff = @Backoff(delay = 1000, multiplier = 2))
	public Issue oppdaterStatus(final String key, @Valid @NotNull JiraTransition transition) {
		HttpHeaders headers = createSecurityHeaders(MediaType.APPLICATION_JSON);
		String url = format("%s/%s%s", issueBaseUri, key, TRANSITIONS);

		try {
			restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(transition, headers), String.class);
			return hentIssue(key);
		} catch (HttpClientErrorException e) {
			log.warn("oppdaterStatus feilet funksjonelt med url={}, status={}, feilmelding={}", url, e.getStatusCode(), e.getMessage());
			throw new JiraFunctionalException(
					format("Kall mot jira feilet funksjonelt med url=%s, status=%s, feilmelding=%s", url, e.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			log.error("oppdaterStatus feilet teknisk med feilmelding={}", e.getMessage());
			throw new JiraTechnicalException(
					format("Kall mot Jira feilet teknisk med statusKode=%s, feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createSecurityHeaders(MediaType mediaType) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Atlassian-Token", "no-check");
		headers.setContentType(mediaType);
		return headers;
	}

}
