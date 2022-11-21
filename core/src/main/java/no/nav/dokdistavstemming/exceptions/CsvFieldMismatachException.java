package no.nav.dokdistavstemming.exceptions;

public class CsvFieldMismatachException extends Exception{

	public CsvFieldMismatachException(String message) {
		super(message);
	}

	public CsvFieldMismatachException(Exception e) {
		super(e);
	}
}
