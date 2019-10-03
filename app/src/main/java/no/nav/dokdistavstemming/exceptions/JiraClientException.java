package no.nav.dokdistavstemming.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Data
@EqualsAndHashCode(callSuper = false)
public class JiraClientException extends HttpClientErrorException {

	public JiraClientException(HttpStatus httpStatus, String  errorMessage) {
		super(httpStatus,errorMessage);
	}


}
