package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostWithDistribusjonsinfo;
import no.nav.dokdistavstemming.domain.EkspedertForsendelse;
import no.nav.dokdistavstemming.domain.EpostVarsel;
import no.nav.dokdistavstemming.domain.SmsVarsel;
import no.nav.dokdistavstemming.domain.enums.UtsendingsKanalCode;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.utils.DataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulkOppdaterDistribusjonsinfoMapperTest {

	private final BulkOppdaterDistribusjonsinfoMapper mapper = new BulkOppdaterDistribusjonsinfoMapper();

	@Test
	public void shouldMapOKBulkOppdaterDistribusjonsinfo() throws IOException {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserFromJson = DataUtils.getHentEkspederteForsendelserFromJson("__files/rdist001/ekspedertforsendelse.json");
		EkspedertForsendelse sdpEkspederteForsendelse = hentEkspederteForsendelserFromJson.getForsendelser().get(0);
		EkspedertForsendelse printEkspederteForsendelse = hentEkspederteForsendelserFromJson.getForsendelser().get(2);

		BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = mapper.map(hentEkspederteForsendelserFromJson);

		JournalpostWithDistribusjonsinfo jpDistInfoSdp = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(0);
		JournalpostWithDistribusjonsinfo jpDistInfoPrint = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(2);

		assertEquals(15, bulkOppdaterDistribusjonsinfoRequest.getJournalposter().size());

		assertEquals(sdpEkspederteForsendelse.getForsendelseId(), jpDistInfoSdp.getForsendelseId());
		assertEquals(Long.valueOf(sdpEkspederteForsendelse.getJournalpostId()), jpDistInfoSdp.getJournalpostId());
		assertEquals(sdpEkspederteForsendelse.getDistribusjonsKanal(), jpDistInfoSdp.getUtsendingsKanal());
		Assertions.assertEquals(sdpEkspederteForsendelse.getDigitalpostkasse().getDigitalpostkasseadresse(), jpDistInfoSdp.getDigitalpostkasse().getDigitalpostkasseadresse());
		Assertions.assertEquals(sdpEkspederteForsendelse.getDigitalpostkasse().getDigitalpostkasseleverandor(), jpDistInfoSdp.getDigitalpostkasse().getDigitalpostkasseleverandor());
		assertTrue(jpDistInfoSdp.getSettStatusEkspedert());
		assertNull(jpDistInfoSdp.getVarsel());
		assertNull(jpDistInfoSdp.getPostadresse());

		assertEquals(Long.valueOf(printEkspederteForsendelse.getJournalpostId()), jpDistInfoPrint.getJournalpostId());
		Assertions.assertEquals(UtsendingsKanalCode.S.name(), jpDistInfoPrint.getUtsendingsKanal());
		Assertions.assertEquals(printEkspederteForsendelse.getPostadresse().getAdresselinje1(), jpDistInfoPrint.getPostadresse().getAdresselinje1());
		Assertions.assertEquals(printEkspederteForsendelse.getPostadresse().getAdresselinje2(), jpDistInfoPrint.getPostadresse().getAdresselinje2());
		Assertions.assertEquals(printEkspederteForsendelse.getPostadresse().getAdresselinje3(), jpDistInfoPrint.getPostadresse().getAdresselinje3());
		Assertions.assertEquals(printEkspederteForsendelse.getPostadresse().getPostnummer(), jpDistInfoPrint.getPostadresse().getPostnummer());
		Assertions.assertEquals(printEkspederteForsendelse.getPostadresse().getPoststed(), jpDistInfoPrint.getPostadresse().getPoststed());
		Assertions.assertEquals(printEkspederteForsendelse.getPostadresse().getLandkode(), jpDistInfoPrint.getPostadresse().getLandkode());
		assertTrue(jpDistInfoPrint.getSettStatusEkspedert());
		assertNull(jpDistInfoPrint.getVarsel());
		assertNull(jpDistInfoPrint.getDigitalpostkasse());

		EkspedertForsendelse dittNavEkspederteForsendelse = hentEkspederteForsendelserFromJson.getForsendelser().get(14);
		EpostVarsel eksepdertForsendelseEpostVarsel = dittNavEkspederteForsendelse.getVarsel().getEpostvarsel().get(0);
		SmsVarsel ekspedertForsendelseSmsVarsel = dittNavEkspederteForsendelse.getVarsel().getSmsvarsel().get(0);
		JournalpostWithDistribusjonsinfo jpDistInfoDittNav = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(14);

		assertEquals(Long.valueOf(dittNavEkspederteForsendelse.getJournalpostId()), jpDistInfoDittNav.getJournalpostId());
		assertEquals(UtsendingsKanalCode.NAV_NO.name(), jpDistInfoDittNav.getUtsendingsKanal());
		Assertions.assertEquals(eksepdertForsendelseEpostVarsel.getAdresse(), jpDistInfoDittNav.getVarsel().getEpostvarsel().get(0).getAdresse());
		Assertions.assertEquals(eksepdertForsendelseEpostVarsel.getTittel(), jpDistInfoDittNav.getVarsel().getEpostvarsel().get(0).getTittel());
		Assertions.assertEquals(eksepdertForsendelseEpostVarsel.getTekst(), jpDistInfoDittNav.getVarsel().getEpostvarsel().get(0).getTekst());
		Assertions.assertEquals(eksepdertForsendelseEpostVarsel.getTidspunkt(), jpDistInfoDittNav.getVarsel().getEpostvarsel().get(0).getTidspunkt());
		Assertions.assertEquals(ekspedertForsendelseSmsVarsel.getTelefonnummer(), jpDistInfoDittNav.getVarsel().getSmsvarsel().get(0).getTelefonnummer());
		Assertions.assertEquals(ekspedertForsendelseSmsVarsel.getTekst(), jpDistInfoDittNav.getVarsel().getSmsvarsel().get(0).getTekst());
		Assertions.assertEquals(ekspedertForsendelseSmsVarsel.getTidspunkt(), jpDistInfoDittNav.getVarsel().getSmsvarsel().get(0).getTidspunkt());
		assertTrue(jpDistInfoDittNav.getSettStatusEkspedert());
		assertNull(jpDistInfoDittNav.getPostadresse());
		assertNull(jpDistInfoDittNav.getDigitalpostkasse());

		EkspedertForsendelse printLandkode = hentEkspederteForsendelserFromJson.getForsendelser().get(13);
		JournalpostWithDistribusjonsinfo printJpLandkode = bulkOppdaterDistribusjonsinfoRequest.getJournalposter().get(13);

		Assertions.assertEquals(printLandkode.getPostadresse().getAdresselinje1(), printJpLandkode.getPostadresse().getAdresselinje1());
		Assertions.assertEquals(printLandkode.getPostadresse().getAdresselinje2(), printJpLandkode.getPostadresse().getAdresselinje2());
		Assertions.assertEquals(printLandkode.getPostadresse().getAdresselinje3(), printJpLandkode.getPostadresse().getAdresselinje3());
		Assertions.assertEquals(printLandkode.getPostadresse().getPostnummer(), printJpLandkode.getPostadresse().getPostnummer());
		Assertions.assertEquals(printLandkode.getPostadresse().getPoststed(), printJpLandkode.getPostadresse().getPoststed());
		Assertions.assertEquals("??", printJpLandkode.getPostadresse().getLandkode());

	}

	@Test
	public void shouldNotMapWhenJournalpostIdIsNull() throws IOException {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserFromJson = DataUtils.getHentEkspederteForsendelserFromJson("__files/rdist001/ekspedertforsendelse_with_jp_null.json");

		BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = mapper.map(hentEkspederteForsendelserFromJson);

		assertEquals(bulkOppdaterDistribusjonsinfoRequest.getJournalposter().size(), 0);
	}

}