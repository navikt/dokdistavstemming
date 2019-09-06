package no.nav.dokdistavstemming.utils;

import no.nav.dokdistavstemming.mdc.MDCConstants;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Configuration
public class CallIdInterceptor implements ClientHttpRequestInterceptor {
	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		httpRequest.getHeaders().set(MDCConstants.MDC_CALL_ID, MDC.get(MDCConstants.MDC_CALL_ID));
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		httpRequest.getHeaders().add(MDCConstants.MDC_USER_ID, userId == null ? "UKJENT" : userId);
		return clientHttpRequestExecution.execute(httpRequest, body);
	}
}
