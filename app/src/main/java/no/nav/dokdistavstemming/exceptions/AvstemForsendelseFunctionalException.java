package no.nav.dokdistavstemming.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AvstemForsendelseFunctionalException extends RuntimeException {

	private final HttpStatus httpStatus;

	public AvstemForsendelseFunctionalException(String message) {
		super(message);
		this.httpStatus = HttpStatus.BAD_REQUEST;
	}

	public AvstemForsendelseFunctionalException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public AvstemForsendelseFunctionalException(String message, Throwable cause) {
		super(message, cause);
		this.httpStatus = HttpStatus.BAD_REQUEST;
	}


}
