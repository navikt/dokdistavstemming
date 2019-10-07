package no.nav.dokdistavstemming.exceptions;

import com.pep1.jira.client.error.ErrorMessage;
import com.pep1.jira.client.error.JIRAClientException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Data
@EqualsAndHashCode(callSuper = false)
@Getter
public class JiraClientException extends RestClientException {



	private HttpStatus status;
	private ErrorMessage errorMessage;

	public JiraClientException(HttpStatus httpStatus, ErrorMessage errorMessage) {
		super(errorMessage.toString());
		this.status = httpStatus;
		this.errorMessage = errorMessage;
	}

	public JiraClientException(String message) {
		super(message);
	}

	public String toString() {
		return "JiraClientException(status=" + this.getStatus() + ", errorMessage=" + this.getErrorMessage() + ")";
	}



}
