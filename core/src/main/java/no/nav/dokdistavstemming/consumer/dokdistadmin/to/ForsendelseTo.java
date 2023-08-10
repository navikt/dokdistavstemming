package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.enums.DistribusjonsTypeKode;

import java.util.List;

@Builder
@Getter
public class ForsendelseTo{

	private final ForsendelseTo forsendelse;
	@Setter
	String bestillingsId;
	@Setter
	DistribusjonKanalCode distribusjonsKanal;
	@Setter
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
	DistribusjonsTypeKode distribusjonstype;


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
		ArkivSystemCode arkivSystem;
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
		@Setter
		String dokumenttypeId;
	}

	private enum ArkivSystemCode {
		JOARK,
		MIDL_BREVLAGER,
		INGEN
	}

}
