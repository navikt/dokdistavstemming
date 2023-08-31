package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.List;

@Builder
@Getter
public class ForsendelseTo {

	long forsendelseId;
	String bestillingsId;
	String distribusjonsKanal;
	String originalDistribusjonId;
	String konversasjonId;
	String bestillendeFagsystem;
	String tema;
	String forsendelseTittel;
	String batchId;
	String dokumentProdApp;
	Mottaker mottaker;
	ArkivInformasjon arkivInformasjon;
	Postadresse postadresse;
	List<Dokument> dokumenter;
	String distribusjonstype;


	@Value
	@Builder
	public static class Mottaker {
		String mottakerId;
		String mottakerNavn;
		String mottakerType;
	}

	@Value
	@Builder
	public static class ArkivInformasjon {
		String arkivSystem;
		String arkivId;
	}

	@Value
	@Builder
	public static class Postadresse {
		String adresselinje1;
		String adresselinje2;
		String adresselinje3;
		String postnummer;
		String poststed;
		String landkode;
	}

	@Getter
	@Builder
	public static class Dokument {
		String tilknyttetSom;
		String dokumentObjektReferanse;
		String arkivDokumentInfoId;
		String dokumenttypeId;
		Integer rekkefolge;
	}
}
