package no.nav.dokdistavstemming.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ConverterUtils {

	private ConverterUtils() {
	}

	public static OffsetDateTime convertStringToDateTime(String parameter) {

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		LocalDateTime parse = LocalDateTime.parse(parameter, dateTimeFormatter);
		return isBlank(parameter) ? null : OffsetDateTime.of(parse, ZoneId.of("Europe/Oslo").getRules().getOffset(parse));
	}
}
