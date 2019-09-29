package no.nav.dokdistavstemming.utils;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.format.DateTimeFormatter;

public class CustomDeserializer extends LocalDateTimeDeserializer {

	public CustomDeserializer() {
		super(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
}


