package no.nav.dokdistavstemming.exceptions;


public class AvstemForsendelseFunctionalException extends RuntimeException {

	public AvstemForsendelseFunctionalException(String message) {
		super(message);
	}

	public AvstemForsendelseFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
