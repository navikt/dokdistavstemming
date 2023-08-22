package no.nav.dokdistavstemming.utils;

import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTo;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class OpprettForsendelseMapper {

	private static final String DISTRIBUSJON_KANAL_PRINT = "PRINT";
	private static final String DOKUMENTTYPE_ID = "U000001";
	private static final String HOVEDDOKUMENT = "HOVEDDOKUMENT";

	public static ForsendelseTo mapForsendelseToTilOpprettForsendelse(ForsendelseTo hentForsendelseResponse, String bestillingsId) {

		if (hentForsendelseResponse == null) {
			throw new IllegalArgumentException("HentForsendelseResponseTo kan ikke være null");
		}

		//assertThatAllRequiredFieldsArePresent(hentForsendelseResponse);
		AtomicReference<Integer> rekkefolge = new AtomicReference<>(2);

		return ForsendelseTo.builder()
				.bestillingsId(bestillingsId)
				.distribusjonsKanal(DISTRIBUSJON_KANAL_PRINT)
				.tema(hentForsendelseResponse.getTema())
				.forsendelseTittel(hentForsendelseResponse.getForsendelseTittel())
				.bestillendeFagsystem(hentForsendelseResponse.getBestillendeFagsystem())
				.batchId(hentForsendelseResponse.getBatchId())
				.dokumentProdApp(hentForsendelseResponse.getDokumentProdApp())
				.originalDistribusjonId(hentForsendelseResponse.getBestillingsId())
				.mottaker(mapMottakerTo(hentForsendelseResponse.getMottaker()))
				.arkivInformasjon(mapArkivInformasjonTo(hentForsendelseResponse.getArkivInformasjon()))
				.postadresse(mapPostadresse(hentForsendelseResponse.getPostadresse()))
				.dokumenter(hentForsendelseResponse.getDokumenter().stream()
						.map(dokumentTo -> {
							if (isHoveddokument(dokumentTo.getTilknyttetSom())) {
								return mapDokument(dokumentTo, 1);
							} else {
								ForsendelseTo.Dokument dok = mapDokument(dokumentTo, rekkefolge.get());
								rekkefolge.getAndSet(rekkefolge.get() + 1);
								return dok;
							}
						})
						.collect(Collectors.toList()))
				.build();
	}

	private static ForsendelseTo.Dokument mapDokument(ForsendelseTo.Dokument dokument, Integer rekkefolge) {
		return ForsendelseTo.Dokument.builder()
				.tilknyttetSom(dokument.getTilknyttetSom())
				.dokumentObjektReferanse(dokument.getDokumentObjektReferanse())
				.arkivDokumentInfoId(dokument.getArkivDokumentInfoId())
				//.rekkefolge(rekkefolge)
				.dokumenttypeId(DOKUMENTTYPE_ID)
				.build();
	}

	private static ForsendelseTo.Postadresse mapPostadresse(ForsendelseTo.Postadresse postadresse) {
		return isEmpty(postadresse) ? null : ForsendelseTo.Postadresse.builder()
				.adresselinje1(postadresse.getAdresselinje1())
				.adresselinje2(postadresse.getAdresselinje2())
				.adresselinje3(postadresse.getAdresselinje3())
				.postnummer(postadresse.getPostnummer())
				.poststed(postadresse.getPoststed())
				.landkode(postadresse.getLandkode())
				.build();
	}

	private static ForsendelseTo.ArkivInformasjon mapArkivInformasjonTo(ForsendelseTo.ArkivInformasjon arkivInformasjon) {
		return ForsendelseTo.ArkivInformasjon.builder()
				.arkivSystem(arkivInformasjon.getArkivSystem())
				.arkivId(arkivInformasjon.getArkivId())
				.build();
	}

	private static ForsendelseTo.Mottaker mapMottakerTo(ForsendelseTo.Mottaker mottaker) {
		assertNotNull(mottaker);
		return ForsendelseTo.Mottaker.builder()
				.mottakerId(mottaker.getMottakerId())
				.mottakerNavn(mottaker.getMottakerNavn())
				.mottakerType(mottaker.getMottakerType())
				.build();
	}

	private static boolean isHoveddokument(String tilknyttetSom) {
		return HOVEDDOKUMENT.equals(tilknyttetSom);
	}
/*
	private void assertThatAllRequiredFieldsArePresent(ForsendelseTo to) {
		assertNotNull("bestillingsId", to.getBestillingsId());
		assertNotNull("bestillendeFagsystem", to.getBestillendeFagsystem());
		assertNotNull("tema", to.getTema());
		assertNotNull("forsendelsetittel", to.getForsendelseTittel());
		assertNotNull("dokumentProdApp", to.getDokumentProdApp());
		assertNotNull("Mottaker", to.getMottaker());
		assertNotNull("mottaker.mottakerId", to.getMottaker().getMottakerId());
		assertNotNull("mottaker.mottakerNavn", to.getMottaker().getMottakerNavn());
		assertNotNull("mottaker.mottakerType", to.getMottaker().getMottakerType());

		if (to.getArkivInformasjon() != null) {
			assertNotNull("arkivinformasjon.arkivSystem", to.getArkivInformasjon().getArkivSystem());
			assertNotNull("arkivinformasjon.arkivId", to.getArkivInformasjon().getArkivId());
		}

		if (to.getPostadresse() != null) {
			assertNotNull("postadresse.landkode", to.getPostadresse().getLandkode());
		}

		assertThatAtLeastOneDocumentIsPresent(to.getDokumenter());
		to.getDokumenter().forEach(dokumentTo ->
				assertDokument(dokumentTo, to.getArkivInformasjon()));
	}

	private void assertDokument(Dokument dokument, ArkivInformasjon arkivInformasjon) {
		assertNotNull("dokumenter.dokument.tilknyttetSom", dokument.getTilknyttetSom());
		assertNotNull("dokumenter.dokument.dokumentObjektReferanse", dokument.getDokumentObjektReferanse());
		assertNotNull("dokumenter.dokument.dokumenttypeId", dokument.getDokumenttypeId());

		if (arkivInformasjon != null) {
			assertNotNull("dokumenter.dokument.arkivdokumentInfoId", dokument.getArkivDokumentInfoId());
		}
	}

	private void assertThatAtLeastOneDocumentIsPresent(List<Dokument> dokumentList) {
		if (dokumentList == null || dokumentList.isEmpty()) {
			throw new IllegalArgumentException("Ugyldig input: Feltet dokumenter må være en liste som inneholder minst ett dokumnet");
		}
	}*/
}
