package no.nav.dokdistavstemming.sdist004.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class DataUtils {

	public static HentEkspederteForsendelserResponse getHentEkspederteForsendelserFromJson(String file) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		InputStream inputStream = new ClassPathResource(file).getInputStream();
		return objectMapper.readValue( inputStream, HentEkspederteForsendelserResponse.class);
	}
}
