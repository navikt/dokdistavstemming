package no.nav.dokdistavstemming.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvstemEkspederteForsendelserRequest {
	private List<Forsendelse> forsendelser;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Forsendelse {
		private Long forsendelseId;
	}
}