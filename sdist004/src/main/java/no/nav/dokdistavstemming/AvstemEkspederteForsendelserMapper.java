package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResultResponse;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class AvstemEkspederteForsendelserMapper {

	public AvstemEkspederteForsendelserRequest mapAvstemEkspederteForsendelser(HentEkspederteForsendelserResponse ekspederteForsendelserResponse,
																			   JournalpostResultResponse journalpostResultResponse) {
		if (journalpostResultResponse == null || (journalpostResultResponse.getOppdatert() == null || journalpostResultResponse.getOppdatert().isEmpty())) {
			return null;
		}

		List<AvstemEkspederteForsendelserRequest.Forsendelse> forsendelser = journalpostResultResponse.getOppdatert().stream()
				.filter(Objects::nonNull)
				.flatMap(jp -> ekspederteForsendelserResponse.getForsendelser().stream().filter(ekspedertForsendelse ->
						isBlank(jp.getErrormessage()) &&
						valueOf(jp.getJournalpostId()).equals(ekspedertForsendelse.getJournalpostId())))
				.map(ekspederteForsendelse -> AvstemEkspederteForsendelserRequest.Forsendelse.builder()
						.forsendelseId(ekspederteForsendelse.getForsendelseId())
						.build()
				)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		return AvstemEkspederteForsendelserRequest.builder()
				.forsendelser(forsendelser)
				.build();
	}

}
