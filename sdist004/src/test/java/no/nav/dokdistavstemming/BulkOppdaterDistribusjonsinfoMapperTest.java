package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static no.nav.dokdistavstemming.utils.DataUtils.getHentEkspederteForsendelserFromJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BulkOppdaterDistribusjonsinfoMapperTest {

	private BulkOppdaterDistribusjonsinfoMapper mapper;

	@BeforeEach
	public void setUp() {
		mapper = new BulkOppdaterDistribusjonsinfoMapper();
	}

	@Test
	public void shouldMapOKBulkOppdaterDistribusjonsinfo() throws IOException {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserFromJson = getHentEkspederteForsendelserFromJson("__files/rdist001/ekspedertforsendelse.json");

		BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = mapper.map(hentEkspederteForsendelserFromJson);

		assertEquals(14, bulkOppdaterDistribusjonsinfoRequest.getJournalposter().size());
	}

	@Test
	public void shouldNotMapWhenJournalpostIdIsNull() throws IOException {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserFromJson = getHentEkspederteForsendelserFromJson("__files/rdist001/ekspedertforsendelse_with_jp_null.json");

		BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = mapper.map(hentEkspederteForsendelserFromJson);

		assertEquals(bulkOppdaterDistribusjonsinfoRequest.getJournalposter().size(), 0);
	}

}