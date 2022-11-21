package no.nav.dokdistavstemming.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class DataUtils {

	public static HentEkspederteForsendelserResponse getHentEkspederteForsendelserFromJson(String file) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		InputStream inputStream = new ClassPathResource(file).getInputStream();
		return objectMapper.readValue( inputStream, HentEkspederteForsendelserResponse.class);
	}
}
