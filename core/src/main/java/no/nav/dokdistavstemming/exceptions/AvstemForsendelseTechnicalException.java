package no.nav.dokdistavstemming.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ResponseStatus(INTERNAL_SERVER_ERROR)
public class AvstemForsendelseTechnicalException extends RuntimeException {

	private final HttpStatus httpStatus;


	public AvstemForsendelseTechnicalException(String message) {
		super(message);
		this.httpStatus = INTERNAL_SERVER_ERROR;
	}

	public AvstemForsendelseTechnicalException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public AvstemForsendelseTechnicalException(String message, Throwable cause) {
		super(message, cause);
		this.httpStatus = INTERNAL_SERVER_ERROR;
	}

	public AvstemForsendelseTechnicalException(String message, Throwable cause, HttpStatus httpStatus) {
		super(message, cause);
		this.httpStatus = INTERNAL_SERVER_ERROR;
	}

}
