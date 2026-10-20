package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.JiraAuthProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class JiraCloudClient {

	private final RestClient restClient;

	public JiraCloudClient(RestClient.Builder restClientBuilder,
						   DokdistavstemmingProperties dokdistavstemmingProperties,
						   JiraAuthProperties jiraAuthProperties) {
		this.restClient = restClientBuilder
				.baseUrl(dokdistavstemmingProperties.getJira().getUrl())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setBearerAuth(jiraAuthProperties.accessToken());
					httpHeaders.set("X-Atlassian-Token", "no-check");
					httpHeaders.setContentType(APPLICATION_JSON);
				})
				.requestInterceptor((request, body, execution) ->
						new JiraResponseDiagnostics(execution.execute(request, body)))
				.build();
	}

	public Project hentJiraCloudProject() {
		return restClient.get()
				.uri("/rest/api/3/project/MMA")
				.accept(APPLICATION_JSON)
				.retrieve()
				.onStatus(HttpStatusCode::is3xxRedirection, (_, response) -> {
					throw new RestClientException("Jira Cloud returnerte HTTP %s i stedet for prosjektdata. Kontroller URL og proxy."
							.formatted(response.getStatusCode().value()));
				})
				.onStatus(status -> status.value() == UNAUTHORIZED.value() || status.value() == FORBIDDEN.value()
						|| status.value() == NOT_FOUND.value(), (_, response) -> {
					String message = switch (response.getStatusCode().value()) {
						case 401 -> "Jira Cloud avviste autentisering. Kontroller jira.cloud.username (Atlassian-e-post), jira.cloud.access-token (API-token) og URL for tokentypen.";
						case 403 -> "Jira Cloud nektet tilgang. Kontroller kontotilgang til prosjektet og API-tokenets scopes.";
						default -> "Jira Cloud fant ikke prosjektet MMA eller skjuler det for kontoen. Kontroller Jira-nettsted/cloudId, prosjektnokkel, autentisering og Browse Projects-tilgang.";
					};
					throw new HttpClientErrorException(response.getStatusCode(), message);
				})
				.body(Project.class);
	}

}
