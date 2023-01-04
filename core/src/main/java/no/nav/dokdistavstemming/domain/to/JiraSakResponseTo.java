package no.nav.dokdistavstemming.domain.to;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiraSakResponseTo {

	private String jiraSakKey;
	private String message;
	private int httpStatusCode;
	private HttpStatus status;
}
