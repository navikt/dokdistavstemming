package no.nav.dokdistavstemming.sdist004;

import no.nav.dokdistavstemming.consumer.dokdistadmin.to.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResultResponse;
import no.nav.dokdistavstemming.domain.EkspedertForsendelse;
import no.nav.dokdistavstemming.domain.Forsendelse;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class AvstemEkspederteForsendelserMapper {

	public AvstemEkspederteForsendelserRequest mapAvstemEkspederteForsendelser(HentEkspederteForsendelserResponse ekspederteForsendelserResponse,
																			   JournalpostResultResponse journalpostResultResponse) {
		if (journalpostResultResponse == null || (journalpostResultResponse.getOppdatert() == null || journalpostResultResponse.getOppdatert().isEmpty())) {
			return null;
		}

		Set<Long> forsendelsesIderSomSkalOppdateres = journalpostResultResponse.getOppdatert().stream()
				.flatMap(journalpost -> ekspederteForsendelserResponse.getForsendelser().stream()
						.filter(ekspedertForsendelse -> ekspedertForsendelse.getJournalpostId().equals(journalpost.getJournalpostId().toString()))
						.map(EkspedertForsendelse::getForsendelseId))
				.collect(toUnmodifiableSet());

		List<Forsendelse> forsendelseList = forsendelsesIderSomSkalOppdateres.stream()
				.map(Forsendelse::new)
				.toList();

		return AvstemEkspederteForsendelserRequest.builder()
				.forsendelser(forsendelseList)
				.build();
	}

}
