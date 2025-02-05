package no.nav.dokdistavstemming.sdist006;

import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static no.nav.dokdistavstemming.sdist006.OpprettForsendelseMapper.mapForsendelseToTilOpprettForsendelse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpprettForsendelseMapperTest {

	private static final String BESTILLINGS_ID = UUID.randomUUID().toString();
	private static final String OLD_BESTILLINGS_ID = UUID.randomUUID().toString();
	private static final String BATCH_ID = "batchId";
	private static final String BESTILLENDE_FAGSYSTEM = "bestillendeFagsystem";
	private static final String TEMA = "FS22";
	private static final String FORSENDELSE_TITTEL = "forsendelseTittel";
	private static final String ARKIV_SYSTEM = "JOARK";
	private static final String ARKIV_ID = "arkivId";
	private static final String MOTTAKER_ID_NAVN = "mottakerIdNavn";
	private static final String MOTTAKER_ID = "mottakerId";
	private static final String ADRESSELINJE_1 = "adresselinje1";
	private static final String ADRESSELINJE_2 = "adresselinje2";
	private static final String ADRESSELINJE_3 = "adresselinje3";
	private static final String POSTNUMMER = "postnummer";
	private static final String POSTSTED = "poststed";
	private static final String LAND = "land";
	private static final String DOKUMENT_PROD_APP = "dokumentProdApp";
	private static final String DOKUMENTTYPE_ID_1 = "U000001";
	private static final String DOKUMENTTYPE_ID_2 = "U000001";
	private static final String OBJEKT_REFERANSE_1 = "objektReferanse1";
	private static final String OBJEKT_REFERANSE_2 = "objektReferanse2";
	private static final String TILKNYTTET_SOM_HOVEDDOK = "HOVEDDOKUMENT";
	private static final String TILKNYTTET_SOM_VEDLEGG = "VEDLEGG";
	private static final String ARKIV_DOKUMENTINFO_ID_1 = "arkivDokumentinfoId1";
	private static final String ARKIV_DOKUMENTINFO_ID_2 = "arkivDokumentinfoId2";

	@Test
	public void shouldMapForsendelser() {
		ForsendelseTo request = mapForsendelseToTilOpprettForsendelse(createHentForsendelseResponse(), BESTILLINGS_ID);

		assertEquals(BESTILLINGS_ID, request.getBestillingsId());
		assertEquals(FORSENDELSE_TITTEL, request.getForsendelseTittel());
		assertEquals(BATCH_ID, request.getBatchId());
		assertEquals(DOKUMENT_PROD_APP, request.getDokumentProdApp());
		assertEquals(BESTILLENDE_FAGSYSTEM, request.getBestillendeFagsystem());
		assertEquals(ARKIV_ID, request.getArkivInformasjon().getArkivId());
		assertEquals(MOTTAKER_ID, request.getMottaker().getMottakerId());
		assertEquals(MOTTAKER_ID_NAVN, request.getMottaker().getMottakerNavn());
		assertEquals(OLD_BESTILLINGS_ID, request.getOriginalDistribusjonId());
		assertPostadresseTo(request.getPostadresse());
		assertDokument(request.getDokumenter().get(1));
	}

	@Test
	public void shouldMapForsendelserWhenAdresseErNull() {
		ForsendelseTo hentForsendelseResponse = createHentForsendelseResponseWithPostadresseNull();
		ForsendelseTo request = mapForsendelseToTilOpprettForsendelse(hentForsendelseResponse, BESTILLINGS_ID);

		assertEquals(BESTILLINGS_ID, request.getBestillingsId());
		assertEquals(FORSENDELSE_TITTEL, request.getForsendelseTittel());
		assertEquals(BATCH_ID, request.getBatchId());
		assertEquals(DOKUMENT_PROD_APP, request.getDokumentProdApp());
		assertEquals(BESTILLENDE_FAGSYSTEM, request.getBestillendeFagsystem());
		assertEquals(ARKIV_ID, request.getArkivInformasjon().getArkivId());
		assertEquals(MOTTAKER_ID, request.getMottaker().getMottakerId());
		assertEquals(MOTTAKER_ID_NAVN, request.getMottaker().getMottakerNavn());
		assertEquals(OLD_BESTILLINGS_ID, request.getOriginalDistribusjonId());
		assertNull(request.getPostadresse());
		assertDokument(request.getDokumenter().get(1));
	}

	@Test
	public void shouldThrowExceptionIfHentForsendelseResponseIsNull() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mapForsendelseToTilOpprettForsendelse(null, BESTILLINGS_ID));
		assertEquals("HentForsendelseResponseTo kan ikke være null", exception.getMessage());
	}

	@Test
	public void shouldThrowExceptionIfMottakerIsNull() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mapForsendelseToTilOpprettForsendelse(createHentForsendelseResponseWithMottakerNull(), BESTILLINGS_ID));
		assertEquals("HV000116: must not be null.", exception.getMessage());
	}

	private void assertPostadresseTo(ForsendelseTo.Postadresse postadresse) {
		assertEquals(ADRESSELINJE_1, postadresse.getAdresselinje1());
		assertEquals(ADRESSELINJE_2, postadresse.getAdresselinje2());
		assertEquals(ADRESSELINJE_3, postadresse.getAdresselinje3());
		assertEquals(POSTNUMMER, postadresse.getPostnummer());
		assertEquals(POSTSTED, postadresse.getPoststed());
		assertEquals(LAND, postadresse.getLandkode());
	}

	private void assertDokument(ForsendelseTo.Dokument dokumentTo) {
		assertEquals(DOKUMENTTYPE_ID_2, dokumentTo.getDokumenttypeId());
		assertEquals(OBJEKT_REFERANSE_2, dokumentTo.getDokumentObjektReferanse());
		assertEquals(TILKNYTTET_SOM_VEDLEGG, dokumentTo.getTilknyttetSom());
		//assertEquals(dokumentTo.getRekkefolge(), 2);
		assertEquals(ARKIV_DOKUMENTINFO_ID_2, dokumentTo.getArkivDokumentInfoId());
	}

	private ForsendelseTo createHentForsendelseResponse() {
		return ForsendelseTo.builder()
				.bestillingsId(OLD_BESTILLINGS_ID)
				.tema(TEMA)
				.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
				.batchId(BATCH_ID)
				.forsendelseTittel(FORSENDELSE_TITTEL)
				.dokumentProdApp(DOKUMENT_PROD_APP)
				.arkivInformasjon(ForsendelseTo.ArkivInformasjon.builder()
						.arkivSystem(ARKIV_SYSTEM)
						.arkivId(ARKIV_ID).build())
				.mottaker(createMottakerTo())
				.postadresse(createPostadresse())
				.dokumenter(createDokument())
				.build();
	}

	private ForsendelseTo createHentForsendelseResponseWithMottakerNull() {
		return ForsendelseTo.builder()
				.bestillingsId(OLD_BESTILLINGS_ID)
				.tema(TEMA)
				.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
				.batchId(BATCH_ID)
				.forsendelseTittel(FORSENDELSE_TITTEL)
				.dokumentProdApp(DOKUMENT_PROD_APP)
				.arkivInformasjon(ForsendelseTo.ArkivInformasjon.builder()
						.arkivId(ARKIV_ID).build())
				.mottaker(null)
				.postadresse(createPostadresse())
				.dokumenter(createDokument())
				.build();
	}

	private ForsendelseTo createHentForsendelseResponseWithPostadresseNull() {
		return ForsendelseTo.builder()
				.bestillingsId(OLD_BESTILLINGS_ID)
				.tema(TEMA)
				.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
				.batchId(BATCH_ID)
				.forsendelseTittel(FORSENDELSE_TITTEL)
				.dokumentProdApp(DOKUMENT_PROD_APP)
				.arkivInformasjon(ForsendelseTo.ArkivInformasjon.builder()
						.arkivSystem(ARKIV_SYSTEM)
						.arkivId(ARKIV_ID).build())
				.mottaker(createMottakerTo())
				.postadresse(null)
				.dokumenter(createDokument())
				.build();
	}

	private List<ForsendelseTo.Dokument> createDokument() {

		return Arrays.asList(
				ForsendelseTo.Dokument.builder()
						.dokumenttypeId(DOKUMENTTYPE_ID_1)
						.dokumentObjektReferanse(OBJEKT_REFERANSE_1)
						.tilknyttetSom(TILKNYTTET_SOM_HOVEDDOK)
						.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_1)
						.build(),
				ForsendelseTo.Dokument.builder()
						.dokumenttypeId(DOKUMENTTYPE_ID_2)
						.dokumentObjektReferanse(OBJEKT_REFERANSE_2)
						.tilknyttetSom(TILKNYTTET_SOM_VEDLEGG)
						.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_2)
						.build(),
				ForsendelseTo.Dokument.builder()
						.dokumenttypeId("1234")
						.dokumentObjektReferanse(OBJEKT_REFERANSE_1)
						.tilknyttetSom(TILKNYTTET_SOM_VEDLEGG)
						.arkivDokumentInfoId(ARKIV_DOKUMENTINFO_ID_1)
						.build());


	}

	private ForsendelseTo.Postadresse createPostadresse() {
		return ForsendelseTo.Postadresse.builder()
				.adresselinje1(ADRESSELINJE_1)
				.adresselinje2(ADRESSELINJE_2)
				.adresselinje3(ADRESSELINJE_3)
				.postnummer(POSTNUMMER)
				.poststed(POSTSTED)
				.landkode(LAND)
				.build();
	}

	private ForsendelseTo.Mottaker createMottakerTo() {
		return ForsendelseTo.Mottaker.builder()
				.mottakerNavn(MOTTAKER_ID_NAVN)
				.mottakerId(MOTTAKER_ID)
				.mottakerType("PERSON")
				.build();
	}
}