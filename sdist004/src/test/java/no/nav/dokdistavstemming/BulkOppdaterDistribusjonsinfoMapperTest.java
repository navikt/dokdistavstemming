package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostWithDistribusjonsinfo;
import no.nav.dokdistavstemming.domain.EkspedertForsendelse;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.domain.UtsendingsKanalCode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static no.nav.dokdistavstemming.utils.DataUtils.getHentEkspederteForsendelserFromJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulkOppdaterDistribusjonsinfoMapperTest {

	private BulkOppdaterDistribusjonsinfoMapper mapper = new BulkOppdaterDistribusjonsinfoMapper();

	@Test
	public void shouldMapOKBulkOppdaterDistribusjonsinfo() throws IOException {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserFromJson = getHentEkspederteForsendelserFromJson("__files/rdist001/ekspedertforsendelse.json");
		EkspedertForsendelse sdpEkspederteForsendelse = hentEkspederteForsendelserFromJson.getForsendelser().get(0);
		EkspedertForsendelse printEkspederteForsendelse = hentEkspederteForsendelserFromJson.getForsendelser().get(2);

		BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = mapper.map(hentEkspederteForsendelserFromJson);

		JournalpostWithDistribusjonsinfo jpDistInfoSdp = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(0);
		JournalpostWithDistribusjonsinfo jpDistInfoPrint = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(2);

		assertEquals(13, bulkOppdaterDistribusjonsinfoRequest.getJournalposter().size());

		assertEquals(sdpEkspederteForsendelse.getForsendelseId(), jpDistInfoSdp.getForsendelseId());
		assertEquals(Long.valueOf(sdpEkspederteForsendelse.getJournalpostId()), jpDistInfoSdp.getJournalpostId());
		assertEquals(sdpEkspederteForsendelse.getDistribusjonsKanal(), jpDistInfoSdp.getUtsendingsKanal());
		assertEquals(sdpEkspederteForsendelse.getDigitalpostkasse().getDigitalpostkasseadresse(), jpDistInfoSdp.getDigitalpostkasse().getDigitalpostkasseadresse());
		assertEquals(sdpEkspederteForsendelse.getDigitalpostkasse().getDigitalpostkasseleverandor(), jpDistInfoSdp.getDigitalpostkasse().getDigitalpostkasseleverandor());
		assertTrue(jpDistInfoSdp.getSettStatusEkspedert());
		assertNull(jpDistInfoSdp.getVarsel());
		assertNull(jpDistInfoSdp.getPostadresse());

		assertEquals(Long.valueOf(printEkspederteForsendelse.getJournalpostId()), jpDistInfoPrint.getJournalpostId());
		assertEquals(UtsendingsKanalCode.S.name(), jpDistInfoPrint.getUtsendingsKanal());
		assertEquals(printEkspederteForsendelse.getPostadresse().getAdresselinje1(), jpDistInfoPrint.getPostadresse().getAdresselinje1());
		assertEquals(printEkspederteForsendelse.getPostadresse().getAdresselinje2(), jpDistInfoPrint.getPostadresse().getAdresselinje2());
		assertEquals(printEkspederteForsendelse.getPostadresse().getAdresselinje3(), jpDistInfoPrint.getPostadresse().getAdresselinje3());
		assertEquals(printEkspederteForsendelse.getPostadresse().getPostnummer(), jpDistInfoPrint.getPostadresse().getPostnummer());
		assertEquals(printEkspederteForsendelse.getPostadresse().getPoststed(), jpDistInfoPrint.getPostadresse().getPoststed());
		assertEquals(printEkspederteForsendelse.getPostadresse().getLandkode(), jpDistInfoPrint.getPostadresse().getLandkode());
		assertTrue(jpDistInfoPrint.getSettStatusEkspedert());
		assertNull(jpDistInfoPrint.getVarsel());
		assertNull(jpDistInfoPrint.getDigitalpostkasse());

		EkspedertForsendelse dittNavEkspederteForsendelse = hentEkspederteForsendelserFromJson.getForsendelser().get(13);
		JournalpostWithDistribusjonsinfo jpDistInfoDittNav = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(12);

		assertEquals(Long.valueOf(dittNavEkspederteForsendelse.getJournalpostId()), jpDistInfoDittNav.getJournalpostId());
		assertEquals(UtsendingsKanalCode.NAV_NO.name(), jpDistInfoDittNav.getUtsendingsKanal());
		assertEquals(dittNavEkspederteForsendelse.getVarsel().getVarseltekst(), jpDistInfoDittNav.getVarsel().getVarseltekst());
		assertEquals(dittNavEkspederteForsendelse.getVarsel().getDigitalkontaktinformasjon(), jpDistInfoDittNav.getVarsel().getDigitalkontaktinformasjon());
		assertTrue(jpDistInfoDittNav.getSettStatusEkspedert());
		assertNull(jpDistInfoDittNav.getPostadresse());
		assertNull(jpDistInfoDittNav.getDigitalpostkasse());
	}

	@Test
	public void shouldNotMapWhenJournalpostIdIsNull() throws IOException {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserFromJson = getHentEkspederteForsendelserFromJson("__files/rdist001/ekspedertforsendelse_with_jp_null.json");

		BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = mapper.map(hentEkspederteForsendelserFromJson);

		assertEquals(bulkOppdaterDistribusjonsinfoRequest.getJournalposter().size(), 0);
	}

}