package no.nav.dokdistavstemming.utils;

import java.util.List;
import java.util.stream.Stream;

public final class LoggingUtils {
	private static final int MAKS_ELEMENTER_LOGGING = 200;

	private LoggingUtils() {
		// ingen instansiering
	}

	public static String trunkertListeToString(List<?> liste) {
		if (liste.size() > MAKS_ELEMENTER_LOGGING) {
			return Stream.concat(liste.stream().limit(MAKS_ELEMENTER_LOGGING), Stream.of("trunkert til maks " + MAKS_ELEMENTER_LOGGING + " elementer...")).toList().toString();
		} else {
			return liste.toString();
		}
	}
}
