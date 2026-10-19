package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.JiraAuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.util.Objects.requireNonNull;
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
					httpHeaders.setBasicAuth(jiraAuthProperties.username(), jiraAuthProperties.accessToken());
					httpHeaders.set("X-Atlassian-Token", "no-check");
					httpHeaders.setContentType(APPLICATION_JSON);
				})
				.build();
	}

	public Project hentJiraCloudProject() {
		return requireNonNull(restClient.get()
				.uri("/rest/api/3/project/MMA")
				.accept(APPLICATION_JSON)
				.retrieve()
				.body(Project.class), "Jira Cloud returnerte en tom prosjektrespons");
	}

}
