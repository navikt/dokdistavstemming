package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResultResponse;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;

import java.util.List;
import java.util.Objects;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class AvstemEkspederteForsendelserMapper {

	public AvstemEkspederteForsendelserRequest mapAvstemEkspederteForsendelser(HentEkspederteForsendelserResponse ekspederteForsendelserResponse,
																			   JournalpostResultResponse journalpostResultResponse) {
		if (journalpostResultResponse.getOppdatert()==null || journalpostResultResponse.getOppdatert().isEmpty()) {
			return null;
		}

		List<AvstemEkspederteForsendelserRequest.Forsendelse> forsendelser = ekspederteForsendelserResponse.getForsendelser().stream()
				.filter(Objects::nonNull)
				.map(ekspederteForsendelse ->
						bulkOppdaterDistribusjonsinfo(journalpostResultResponse, ekspederteForsendelse.getJournalpostId(), ekspederteForsendelse.getForsendelseId()))
				.toList();

		return AvstemEkspederteForsendelserRequest.builder()
				.forsendelser(forsendelser)
				.build();
	}

	public AvstemEkspederteForsendelserRequest.Forsendelse bulkOppdaterDistribusjonsinfo(JournalpostResultResponse journalpostResultResponse,
																						 String journalpostId, Long forsendelseId) {
		return journalpostResultResponse.getOppdatert().stream()
				.filter(jp -> isBlank(jp.getErrormessage()) && valueOf(jp.getJournalpostId()).equals(journalpostId))
				.map(journalpostResponse -> AvstemEkspederteForsendelserRequest.Forsendelse.builder()
						.forsendelseId(forsendelseId)
						.build())
				.findAny().orElse(null);
	}
}
