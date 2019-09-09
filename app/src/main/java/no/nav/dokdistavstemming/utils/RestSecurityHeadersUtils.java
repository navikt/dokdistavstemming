package no.nav.dokdistavstemming.utils;

import no.nav.freg.security.oidc.auth.common.OidcTokenAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public final class RestSecurityHeadersUtils {

	private RestSecurityHeadersUtils() {
		throw new IllegalStateException("Utility class");
	}


	public static HttpHeaders createOidcHeadersOnlyIdToken() {
		String oidcBearerToken = "Bearer " + ((OidcTokenAuthentication) SecurityContextHolder.getContext()
				.getAuthentication()).getIdToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, oidcBearerToken);
		return headers;
	}


	public static HttpHeaders createHeadersWithBasicAuth(String username, String password) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBasicAuth(username, password);
		return headers;
	}
}
