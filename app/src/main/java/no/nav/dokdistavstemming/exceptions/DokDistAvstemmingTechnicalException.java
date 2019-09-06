package no.nav.dokdistavstemming.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DokDistAvstemmingTechnicalException extends RuntimeException {

	private final HttpStatus httpStatus;


	public DokDistAvstemmingTechnicalException(String message) {
		super(message);
		this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public DokDistAvstemmingTechnicalException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public DokDistAvstemmingTechnicalException(String message, Throwable cause) {
		super(message, cause);
		this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public DokDistAvstemmingTechnicalException(String message, Throwable cause, HttpStatus httpStatus) {
		super(message, cause);
		this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
	}

}
