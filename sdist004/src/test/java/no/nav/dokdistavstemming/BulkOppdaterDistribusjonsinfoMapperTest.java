package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostWithDistribusjonsinfo;
import no.nav.dokdistavstemming.domain.EkspedertForsendelse;
import no.nav.dokdistavstemming.domain.Epostvarsel;
import no.nav.dokdistavstemming.domain.Smsvarsel;
import no.nav.dokdistavstemming.domain.enums.UtsendingsKanalCode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static no.nav.dokdistavstemming.utils.DataUtils.getHentEkspederteForsendelserFromJson;
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
		Epostvarsel ekspedertForsendelseEpostvarsel = dittNavEkspedertForsendelse.getVarsel().getEpostvarsel().get(0);
		Smsvarsel ekspedertForsendelseSmsvarsel = dittNavEkspedertForsendelse.getVarsel().getSmsvarsel().get(0);
		JournalpostWithDistribusjonsinfo jpDistInfoDittNav = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(14);

		assertEquals(Long.valueOf(dittNavEkspedertForsendelse.getJournalpostId()), jpDistInfoDittNav.getJournalpostId());
		assertEquals(UtsendingsKanalCode.NAV_NO.name(), jpDistInfoDittNav.getUtsendingsKanal());
		assertEquals(ekspedertForsendelseEpostvarsel.getAdresse(), jpDistInfoDittNav.getVarsel().getEpostvarsel().get(0).getAdresse());
		assertEquals(ekspedertForsendelseEpostvarsel.getTittel(), jpDistInfoDittNav.getVarsel().getEpostvarsel().get(0).getTittel());
		assertEquals(ekspedertForsendelseEpostvarsel.getTekst(), jpDistInfoDittNav.getVarsel().getEpostvarsel().get(0).getTekst());
		assertEquals(ekspedertForsendelseEpostvarsel.getTidspunkt(), jpDistInfoDittNav.getVarsel().getEpostvarsel().get(0).getTidspunkt());
		assertEquals(ekspedertForsendelseSmsvarsel.getTelefonnummer(), jpDistInfoDittNav.getVarsel().getSmsvarsel().get(0).getTelefonnummer());
		assertEquals(ekspedertForsendelseSmsvarsel.getTekst(), jpDistInfoDittNav.getVarsel().getSmsvarsel().get(0).getTekst());
		assertEquals(ekspedertForsendelseSmsvarsel.getTidspunkt(), jpDistInfoDittNav.getVarsel().getSmsvarsel().get(0).getTidspunkt());
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

		assertEquals(bulkOppdaterDistribusjonsinfoRequest.getJournalposter().size(), 0);
	}

}