package no.nav.dokdistavstemming.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConverterUtils {

    private ConverterUtils() {
    }

    public static LocalDateTime convertStringToLocalDateTime(String parameter) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return parameter == null ? null : LocalDateTime.parse(parameter, dateTimeFormatter);
    }

    public static <T extends Enum<T>> T stringToEnum(String value, Class<T> clazz) {
        if (value == null) {
            return null;
        }

        return Enum.valueOf(clazz, value);
    }

}
