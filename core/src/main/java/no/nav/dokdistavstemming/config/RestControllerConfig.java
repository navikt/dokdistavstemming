package no.nav.dokdistavstemming.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RestControllerConfig implements WebMvcConfigurer {

	final SporingInterceptor sporingInterceptor;

	public RestControllerConfig(SporingInterceptor sporingInterceptor) {
		this.sporingInterceptor = sporingInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(sporingInterceptor).addPathPatterns("/rest/**");
	}

}
