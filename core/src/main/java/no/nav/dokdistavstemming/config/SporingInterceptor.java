package no.nav.dokdistavstemming.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class SporingInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		hentHeadersNavn(request);
		return true;
	}

	private void hentHeadersNavn(HttpServletRequest request) {

		request.getHeaderNames().asIterator()
				.forEachRemaining(headerName ->
						log.info("Header for test: {} = {}", headerName, request.getHeader(headerName))
				);
	}
}
