package no.nav.dokdistavstemming.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokdistavstemming.domain.EkspederteForsendelse;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class DataUtils {


	public HentEkspederteForsendelserResponse createHentEkspederteForsendelserResponse() {

		return HentEkspederteForsendelserResponse.builder()
				.forsendelser(null)
				.build();
	}

	public EkspederteForsendelse createEkspederteForsendelse() {

	/*	return EkspederteForsendelse.builder()
				.forsendelseId()
				.journalpostId()
				.distribusjonsKanal()
				.ekspedertDato()
				.digitalpostkasse()
				.postadresse()
				.varsel()
				.build();*/
		return null;
	}



	public static HentEkspederteForsendelserResponse getHentEkspederteForsendelserFromJson(String file) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		InputStream inputStream = new ClassPathResource(file).getInputStream();
		return objectMapper.readValue( inputStream, HentEkspederteForsendelserResponse.class);
	}
}
