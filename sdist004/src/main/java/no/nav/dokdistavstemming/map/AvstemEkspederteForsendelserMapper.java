package no.nav.dokdistavstemming.map;

import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;

import java.util.List;
import java.util.stream.Collectors;

public class AvstemEkspederteForsendelserMapper {

	public AvstemEkspederteForsendelserRequest mapAvstemEkspederteForsendelser(HentEkspederteForsendelserResponse ekspederteForsendelserResponse) {
		List<AvstemEkspederteForsendelserRequest.Forsendelse> forsendelser = ekspederteForsendelserResponse.getForsendelser().stream()
				.map(ekspederteForsendelse ->
						AvstemEkspederteForsendelserRequest.Forsendelse.builder()
								.forsendelseId(Long.valueOf(ekspederteForsendelse.getForsendelseId()))
								.build()
				).collect(Collectors.toList());

		return AvstemEkspederteForsendelserRequest.builder()
				.forsendelser(forsendelser)
				.build();
	}
}
