package no.nav.dokdistavstemming.utils;



import io.micrometer.core.instrument.util.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TestUtils {

	private static final String DATE_FORMATTER = "yyyy-MM-dd HH:mm:ss";

	public static String classpathToString(String path) throws IOException {
		return IOUtils.toString(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);
	}


	public static LocalDateTime convertStringToLocalDateTime(String parameter){
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
		return LocalDateTime.parse(parameter,dateTimeFormatter);
	}

	public static String convertDateTimeToString(LocalDateTime localDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
		return localDateTime.format(formatter);

	}


}