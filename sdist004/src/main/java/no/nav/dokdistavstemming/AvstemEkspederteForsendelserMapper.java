package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResultResponse;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
				.map(ekspederteForsendelse ->
						bulkOppdaterDistribusjonsinfo(journalpostResultResponse, ekspederteForsendelse.getJournalpostId(), ekspederteForsendelse.getForsendelseId())
				)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		return AvstemEkspederteForsendelserRequest.builder()
				.forsendelser(forsendelser)
				.build();
	}

	private AvstemEkspederteForsendelserRequest.Forsendelse bulkOppdaterDistribusjonsinfo(JournalpostResultResponse journalpostResultResponse,
																						  String journalpostId, Long forsendelseId) {
		return journalpostResultResponse == null ? null : mergeOppdetertJpAndStatusEkspedert(journalpostResultResponse).stream()
				.filter(jp -> valueOf(jp.getJournalpostId()).equals(journalpostId))
				.map(journalpostResponse -> AvstemEkspederteForsendelserRequest.Forsendelse.builder()
						.forsendelseId(forsendelseId)
						.build())
				.findAny().orElse(null);
	}

	private List<JournalpostResponse> mergeOppdetertJpAndStatusEkspedert(JournalpostResultResponse journalpostResultResponse) {
		if (journalpostResultResponse.getFeilet() == null && journalpostResultResponse.getOppdatert() != null) {
			return journalpostResultResponse.getOppdatert().stream()
					.filter(jp -> isBlank(jp.getErrormessage()))
					.toList();
		} else {
			if (journalpostResultResponse.getOppdatert() == null) {
				return journalpostResultResponse.getFeilet().stream()
						.filter(this::isJournalpostStatusEkspedert).toList();
			}
			return Stream.of(journalpostResultResponse.getOppdatert().stream()
					.filter(jp -> isBlank(jp.getErrormessage()))
					.toList().stream(), journalpostResultResponse.getFeilet().stream()
					.filter(this::isJournalpostStatusEkspedert).toList().stream())
					.flatMap(Function.identity())
					.collect(Collectors.toList());
		}
	}

	private boolean isJournalpostStatusEkspedert(JournalpostResponse journalpostResponse) {
		return nonNull(journalpostResponse.getErrormessage()) && journalpostResponse.getErrormessage().contains(MELDING_STATUS_EKSPEDERT);
	}
}
