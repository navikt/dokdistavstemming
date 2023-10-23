package no.nav.dokdistavstemming.domain.to;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraTransition {

	private Transition transition;

	@Data
	@Builder
	public static class Transition {
		@NotEmpty
		private String id;
	}

}
