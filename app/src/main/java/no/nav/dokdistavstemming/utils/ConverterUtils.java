package no.nav.dokdistavstemming.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingTechnicalException;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConverterUtils {


	public static LocalDateTime convertStringToLocalDateTime(String parameter) {

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return LocalDateTime.parse(parameter, dateTimeFormatter);
	}

	public static <T extends Enum<T>> T stringToEnum(String value, Class<T> clazz) {
		if (value == null) {
			return null;
		}

		return Enum.valueOf(clazz, value);
	}



	public static <T> List<T> jsonStringToObjectList(String jsonString, Class<T> tClass) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionType(List.class, tClass));
		} catch (IOException e) {
			throw new DokDistAvstemmingTechnicalException(e.getMessage(), e);
		}

	}

	public static String getValueFromJsonString(String body, String parameter) {
		JSONObject jsonObject = convertJsonStringToJsonObject(body);
		return jsonObject == null ? body : (String) jsonObject.get(parameter);
	}

	public static JSONObject convertJsonStringToJsonObject(String body) {
		try {
			return (JSONObject) new JSONParser().parse(body);
		} catch (Exception e) {
			return null;
		}
	}


}
