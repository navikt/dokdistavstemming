package no.nav.dokdistavstemming.consumer.dokumentdistribusjon.jira;


import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;


@Service
public class JiraConsumer {

	private final ServiceuserAlias serviceuserAlias;
	private final RestTemplate restTemplate;

	public JiraConsumer(ServiceuserAlias serviceuserAlias, RestTemplate restTemplate) {
		this.serviceuserAlias = serviceuserAlias;
		this.restTemplate = restTemplate;
	}


	public HttpHeaders oppretteHeaders(MediaType mediaType){
		String base64Credential = new String(Base64.encodeBase64(String.format("%s:%s",serviceuserAlias.getUsername(),serviceuserAlias.getPassword())
				.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic" + base64Credential);
		headers.add("X-Atlassian-Token", "no-check");
		headers.setContentType(mediaType);
		return headers;

	}
}
