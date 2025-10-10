package no.nav.dokdistavstemming.sdist004;

import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostWithDistribusjonsinfo;
import no.nav.dokdistavstemming.domain.EkspedertForsendelse;
import no.nav.dokdistavstemming.domain.Epostvarsel;
import no.nav.dokdistavstemming.domain.Smsvarsel;
import no.nav.dokdistavstemming.domain.enums.UtsendingsKanalCode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static no.nav.dokdistavstemming.domain.enums.UtsendingsKanalCode.NAV_NO;
import static no.nav.dokdistavstemming.sdist004.utils.DataUtils.getHentEkspederteForsendelserFromJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulkOppdaterDistribusjonsinfoMapperTest {

	private final BulkOppdaterDistribusjonsinfoMapper mapper = new BulkOppdaterDistribusjonsinfoMapper();

	@Test
	public void shouldMapOKBulkOppdaterDistribusjonsinfo() throws IOException {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserFromJson = getHentEkspederteForsendelserFromJson("__files/rdist001/ekspedertforsendelse.json");
		EkspedertForsendelse sdpEkspederteForsendelse = hentEkspederteForsendelserFromJson.getForsendelser().get(0);
		EkspedertForsendelse printEkspederteForsendelse = hentEkspederteForsendelserFromJson.getForsendelser().get(2);

		BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = mapper.map(hentEkspederteForsendelserFromJson);

		JournalpostWithDistribusjonsinfo jpDistInfoSdp = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(0);
		JournalpostWithDistribusjonsinfo jpDistInfoPrint = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(2);

		assertEquals(15, bulkOppdaterDistribusjonsinfoRequest.getJournalposter().size());

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

		EkspedertForsendelse dittNavEkspedertForsendelse = hentEkspederteForsendelserFromJson.getForsendelser().get(14);
		Epostvarsel ekspedertForsendelseEpostvarsel = dittNavEkspedertForsendelse.getVarsel().getEpostvarsel().getFirst();
		Smsvarsel ekspedertForsendelseSmsvarsel = dittNavEkspedertForsendelse.getVarsel().getSmsvarsel().getFirst();
		JournalpostWithDistribusjonsinfo jpDistInfoDittNav = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(14);

		assertEquals(Long.valueOf(dittNavEkspedertForsendelse.getJournalpostId()), jpDistInfoDittNav.getJournalpostId());
		assertEquals(NAV_NO.name(), jpDistInfoDittNav.getUtsendingsKanal());
		assertEquals(ekspedertForsendelseEpostvarsel.getAdresse(), jpDistInfoDittNav.getVarsel().epostvarsel().getFirst().epostadresse());
		assertEquals(ekspedertForsendelseEpostvarsel.getTittel(), jpDistInfoDittNav.getVarsel().epostvarsel().getFirst().tittel());
		assertEquals(ekspedertForsendelseEpostvarsel.getTekst(), jpDistInfoDittNav.getVarsel().epostvarsel().getFirst().tekst());
		assertEquals(ekspedertForsendelseEpostvarsel.getTidspunkt(), jpDistInfoDittNav.getVarsel().epostvarsel().getFirst().varslingstidspunkt());
		assertEquals(ekspedertForsendelseSmsvarsel.getTelefonnummer(), jpDistInfoDittNav.getVarsel().smsvarsel().getFirst().mobilnummer());
		assertEquals(ekspedertForsendelseSmsvarsel.getTekst(), jpDistInfoDittNav.getVarsel().smsvarsel().getFirst().tekst());
		assertEquals(ekspedertForsendelseSmsvarsel.getTidspunkt(), jpDistInfoDittNav.getVarsel().smsvarsel().getFirst().varslingstidspunkt());
		assertTrue(jpDistInfoDittNav.getSettStatusEkspedert());
		assertNull(jpDistInfoDittNav.getPostadresse());
		assertNull(jpDistInfoDittNav.getDigitalpostkasse());

		EkspedertForsendelse printLandkode = hentEkspederteForsendelserFromJson.getForsendelser().get(13);
		JournalpostWithDistribusjonsinfo printJpLandkode = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(13);

		assertEquals(printLandkode.getPostadresse().getAdresselinje1(), printJpLandkode.getPostadresse().getAdresselinje1());
		assertEquals(printLandkode.getPostadresse().getAdresselinje2(), printJpLandkode.getPostadresse().getAdresselinje2());
		assertEquals(printLandkode.getPostadresse().getAdresselinje3(), printJpLandkode.getPostadresse().getAdresselinje3());
		assertEquals(printLandkode.getPostadresse().getPostnummer(), printJpLandkode.getPostadresse().getPostnummer());
		assertEquals(printLandkode.getPostadresse().getPoststed(), printJpLandkode.getPostadresse().getPoststed());
		assertEquals("??", printJpLandkode.getPostadresse().getLandkode());

	}

	@Test
	public void shouldNotMapWhenJournalpostIdIsNull() throws IOException {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserFromJson = getHentEkspederteForsendelserFromJson("__files/rdist001/ekspedertforsendelse_with_jp_null.json");

		BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = mapper.map(hentEkspederteForsendelserFromJson);

		assertEquals(0, bulkOppdaterDistribusjonsinfoRequest.getJournalposter().size());
	}

}