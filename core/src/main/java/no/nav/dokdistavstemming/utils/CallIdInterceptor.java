package no.nav.dokdistavstemming.utils;

import no.nav.dokdistavstemming.mdc.MDCConstants;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static no.nav.dokdistavstemming.mdc.MDCConstants.MDC_CALL_ID;
import static no.nav.dokdistavstemming.mdc.MDCConstants.MDC_USER_ID;

@Configuration
public class CallIdInterceptor implements ClientHttpRequestInterceptor {
	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		httpRequest.getHeaders().set(MDC_CALL_ID, MDC.get(MDC_CALL_ID));
		String userId = MDC.get(MDC_USER_ID);
		httpRequest.getHeaders().add(MDC_USER_ID, userId == null ? "UKJENT" : userId);
		return clientHttpRequestExecution.execute(httpRequest, body);
	}
}
