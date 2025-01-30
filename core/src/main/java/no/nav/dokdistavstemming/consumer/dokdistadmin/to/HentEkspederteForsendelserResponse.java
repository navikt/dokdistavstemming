package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import lombok.Builder;
import lombok.Data;
import no.nav.dokdistavstemming.domain.EkspedertForsendelse;

import java.util.List;

@Data
@Builder
public class HentEkspederteForsendelserResponse {
	private final List<EkspedertForsendelse> forsendelser;

	public static HentEkspederteForsendelserResponse empty() {
		return new HentEkspederteForsendelserResponse(List.of());
	}
}
