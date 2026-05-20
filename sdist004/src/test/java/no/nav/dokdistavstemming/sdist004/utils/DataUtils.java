package no.nav.dokdistavstemming.sdist004.utils;

import tools.jackson.databind.json.JsonMapper;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class DataUtils {

	public static HentEkspederteForsendelserResponse getHentEkspederteForsendelserFromJson(String file) throws IOException {
		JsonMapper objectMapper = new JsonMapper();
		InputStream inputStream = new ClassPathResource(file).getInputStream();
		return objectMapper.readValue( inputStream, HentEkspederteForsendelserResponse.class);
	}
}
