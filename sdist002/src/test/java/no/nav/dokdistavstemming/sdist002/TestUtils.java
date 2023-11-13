package no.nav.dokdistavstemming.sdist002;

import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestUtils {
	public static String classpathToString(String path) throws IOException {
		return IOUtils.toString(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);
	}
}