package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResultResponse;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;

import java.util.List;
import java.util.Objects;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class AvstemEkspederteForsendelserMapper {

	private static final String MELDING_STATUS_EKSPEDERT = "Kan ikke ekspedere journalpost med status E";

	public AvstemEkspederteForsendelserRequest mapAvstemEkspederteForsendelser(HentEkspederteForsendelserResponse ekspederteForsendelserResponse,
																			   JournalpostResultResponse journalpostResultResponse) {
		if (journalpostResultResponse == null) {
			return null;
		}

		List<AvstemEkspederteForsendelserRequest.Forsendelse> forsendelser = ekspederteForsendelserResponse.getForsendelser().stream()
				.filter(Objects::nonNull)
				.map(ekspederteForsendelse -> {
					if (journalpostResultResponse.getFeilet() != null) {
						return mapForsendelseIdFraFeilJournalpostStatusE(journalpostResultResponse, ekspederteForsendelse.getJournalpostId(), ekspederteForsendelse.getForsendelseId());
					}
					return bulkOppdaterDistribusjonsinfo(journalpostResultResponse, ekspederteForsendelse.getJournalpostId(), ekspederteForsendelse.getForsendelseId());
				})
				.filter(Objects::nonNull)
				.toList();

		return AvstemEkspederteForsendelserRequest.builder()
				.forsendelser(forsendelser)
				.build();
	}

	public AvstemEkspederteForsendelserRequest.Forsendelse bulkOppdaterDistribusjonsinfo(JournalpostResultResponse journalpostResultResponse,
																						 String journalpostId, Long forsendelseId) {
		return journalpostResultResponse.getOppdatert() == null ? null : journalpostResultResponse.getOppdatert().stream()
				.filter(jp -> isBlank(jp.getErrormessage()) && valueOf(jp.getJournalpostId()).equals(journalpostId))
				.map(journalpostResponse -> AvstemEkspederteForsendelserRequest.Forsendelse.builder()
						.forsendelseId(forsendelseId)
						.build())
				.findAny().orElse(null);
	}

	public AvstemEkspederteForsendelserRequest.Forsendelse mapForsendelseIdFraFeilJournalpostStatusE(JournalpostResultResponse journalpostResultResponse,
																									 String journalpostId, Long forsendelseId) {
		return journalpostResultResponse.getFeilet() == null ? null : journalpostResultResponse.getFeilet().stream()
				.filter(jp -> isJournalpostStatusEkspedert(jp) && valueOf(jp.getJournalpostId()).equals(journalpostId))
				.map(journalpostResponse -> AvstemEkspederteForsendelserRequest.Forsendelse.builder()
						.forsendelseId(forsendelseId)
						.build())
				.findAny().orElse(null);
	}

	private boolean isJournalpostStatusEkspedert(JournalpostResponse journalpostResponse) {
		return nonNull(journalpostResponse.getErrormessage()) && journalpostResponse.getErrormessage().contains(MELDING_STATUS_EKSPEDERT);
	}
}
