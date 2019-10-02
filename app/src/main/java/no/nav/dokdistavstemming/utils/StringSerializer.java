package no.nav.dokdistavstemming.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class StringSerializer extends StdSerializer<String> {


	public StringSerializer() {
		super(String.class);
	}

	@Override
	public void serialize(String value, JsonGenerator generator, SerializerProvider provider) throws IOException {

		generator.writeString(value.format(toString()));

	}
}
