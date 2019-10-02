package no.nav.dokdistavstemming.domain.to;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiraSakResponseTo {

	private String message;
	private int httpStatusCode;
	private HttpStatus status;
}
