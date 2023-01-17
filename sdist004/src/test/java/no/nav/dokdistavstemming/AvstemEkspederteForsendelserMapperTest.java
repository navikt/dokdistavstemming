package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResultResponse;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.EkspedertForsendelse;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class AvstemEkspederteForsendelserMapperTest {

	private final AvstemEkspederteForsendelserMapper avstemEkspederteForsendelserMapper = new AvstemEkspederteForsendelserMapper();

	@Test
	void shouldMapNEkspederteForsendelseToNJournalpostResponse() {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserResponse = createHentEkspederteForsendelser(asList(
				createEkspedertForsendelse(1000, 400000000),
				createEkspedertForsendelse(1001, 400000001),
				createEkspedertForsendelse(1002, 400000002)
		));
		JournalpostResultResponse journalpostResultResponse = createJournalpostResultResponse(asList(
				JournalpostResponse.ok(400000000),
				JournalpostResponse.ok(400000001),
				JournalpostResponse.ok(400000002)
		));

		AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest = avstemEkspederteForsendelserMapper.mapAvstemEkspederteForsendelser(hentEkspederteForsendelserResponse, journalpostResultResponse);

		assertThat(avstemEkspederteForsendelserRequest).isNotNull();
		List<AvstemEkspederteForsendelserRequest.Forsendelse> forsendelser = avstemEkspederteForsendelserRequest.getForsendelser();
		assertThat(forsendelser).hasSize(3);
		assertThat(forsendelser)
				.extracting(AvstemEkspederteForsendelserRequest.Forsendelse::getForsendelseId)
				.containsExactlyInAnyOrder(1000L, 1001L, 1002L);
	}

	@Test
	void shouldMap1To1ForsendelserWhen3Duplicates() {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserResponse = createHentEkspederteForsendelser(asList(
				createEkspedertForsendelse(1000, 400000000),
				createEkspedertForsendelse(1000, 400000000),
				createEkspedertForsendelse(1000, 400000000)
		));
		JournalpostResultResponse journalpostResultResponse = createJournalpostResultResponse(asList(
				JournalpostResponse.ok(400000000),
				JournalpostResponse.ok(400000000),
				JournalpostResponse.ok(400000000)
		));

		AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest = avstemEkspederteForsendelserMapper.mapAvstemEkspederteForsendelser(hentEkspederteForsendelserResponse, journalpostResultResponse);

		assertThat(avstemEkspederteForsendelserRequest).isNotNull();
		List<AvstemEkspederteForsendelserRequest.Forsendelse> forsendelser = avstemEkspederteForsendelserRequest.getForsendelser();
		assertThat(forsendelser).hasSize(1);
		assertThat(forsendelser)
				.extracting(AvstemEkspederteForsendelserRequest.Forsendelse::getForsendelseId)
				.containsExactly(1000L);
	}

	private JournalpostResultResponse createJournalpostResultResponse(List<JournalpostResponse> oppdatert) {
		return JournalpostResultResponse.builder()
				.oppdatert(oppdatert)
				.build();
	}

	private HentEkspederteForsendelserResponse createHentEkspederteForsendelser(List<EkspedertForsendelse> forsendelser) {
		return HentEkspederteForsendelserResponse.builder()
				.forsendelser(forsendelser)
				.build();
	}

	private EkspedertForsendelse createEkspedertForsendelse(long forsendelseId, long journalpostId) {
		return EkspedertForsendelse.builder()
				.forsendelseId(forsendelseId)
				.journalpostId(valueOf(journalpostId))
				.build();
	}
}