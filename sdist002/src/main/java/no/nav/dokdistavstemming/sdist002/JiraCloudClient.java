package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.config.JiraAuthProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

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
				.configureMessageConverters(converters -> {
					// Jira kan returnere JSON uten korrekt Content-Type.
					var converter = new JacksonJsonHttpMessageConverter();
					var mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
					mediaTypes.add(APPLICATION_OCTET_STREAM);
					converter.setSupportedMediaTypes(mediaTypes);
					converters.withJsonConverter(converter);
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
				.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
					throw new RuntimeException(String.format("Jira Cloud returnerte 4xx-feil: %s, %s, body=%s", res.getHeaders(), res.getStatusCode(),
							StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8)));
				})
				.body(Project.class);
	}

}
