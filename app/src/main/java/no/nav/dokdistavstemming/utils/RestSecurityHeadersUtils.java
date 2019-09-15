package no.nav.dokdistavstemming.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class RestSecurityHeadersUtils {
	private RestSecurityHeadersUtils() {
		throw new IllegalStateException("Utility class");
	}


	public static HttpHeaders createHeadersWithBasicAuth(String username, String password) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Atlassian-Token", "no-check");
		headers.setBasicAuth(username, password);
		return headers;
	}

}
