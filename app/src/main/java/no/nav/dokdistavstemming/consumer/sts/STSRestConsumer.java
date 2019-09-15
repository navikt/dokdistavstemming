package no.nav.dokdistavstemming.consumer.sts;

import no.nav.dokdistavstemming.config.alias.ServiceuserAlias;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static no.nav.dokdistavstemming.utils.RestSecurityHeadersUtils.createHeadersWithBasicAuth;

@Component
public class STSRestConsumer {

	private final RestTemplate restTemplate;
	private final String stsUrl;
	private final ServiceuserAlias serviceuserAlias;

	public STSRestConsumer(RestTemplate restTemplate, @Value("${security-token-service-token.url}") String stsUrl, ServiceuserAlias serviceuserAlias) {
		this.restTemplate = restTemplate;
		this.stsUrl = stsUrl;
		this.serviceuserAlias = serviceuserAlias;
	}

	public ResponseEntity<STSResponse> getServiceuserOIDCToken() {
		try {
			HttpHeaders httpHeaders = createHeadersWithBasicAuth(serviceuserAlias.getUsername(), serviceuserAlias.getPassword());
			return restTemplate.exchange(stsUrl + "?grant_type=client_credentials&scope=openid",  HttpMethod.GET, new HttpEntity<>(httpHeaders), STSResponse.class);
		} catch (
				HttpClientErrorException e) {
			throw new DokDistAvstemmingTechnicalException(String.format("Kallet til STS feilet med status=%s feilmelding=%s.", e.getStatusCode(), e.getMessage()), e);
		} catch (
				HttpServerErrorException e) {
			throw new DokDistAvstemmingTechnicalException(String.format("Kallet til STS feilet teknisk med status=%s feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		}
	}
}
