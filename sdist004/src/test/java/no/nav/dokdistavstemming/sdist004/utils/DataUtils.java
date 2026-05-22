package no.nav.dokdistavstemming.sdist004.utils;

import tools.jackson.databind.json.JsonMapper;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class DataUtils {
	private static final JsonMapper OBJECT_MAPPER = new JsonMapper();

	public static HentEkspederteForsendelserResponse getHentEkspederteForsendelserFromJson(String file) throws IOException {
		InputStream inputStream = new ClassPathResource(file).getInputStream();
		return OBJECT_MAPPER.readValue(inputStream, HentEkspederteForsendelserResponse.class);
	}
}
