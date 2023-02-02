package no.nav.dokdistavstemming.exceptions;


public class JiraFunctionalException extends RuntimeException {

	public JiraFunctionalException(String message) {
		super(message);
	}

	public JiraFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
