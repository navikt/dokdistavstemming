package no.nav.dokdistavstemming.exceptions.jiraexception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

@Data
@EqualsAndHashCode(callSuper = false)
public class JIRAClientException extends RestClientException {

	private HttpStatus status;

	private ErrorMessage errorMessage;

	public JIRAClientException(HttpStatus httpStatus, ErrorMessage errorMessage) {
		super(errorMessage.toString());
		this.status = httpStatus;
		this.errorMessage = errorMessage;
	}
}