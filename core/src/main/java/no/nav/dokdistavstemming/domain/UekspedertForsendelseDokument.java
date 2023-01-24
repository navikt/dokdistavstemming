package no.nav.dokdistavstemming.domain;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class UekspedertForsendelseDokument {

	private String distribusjonId;
	private String distribusjonKanal;
	private String distribusjonStatus;
	private String opprettetDato;
	private String distribusjonDato;
	private String forsendelseId;
	private String dokumentId;
	private String dokumentStatus;
	private String konversasjonId;
	private String bestillendeFagsystem;
	private String fagomradeCode;
	private String journalpostId;
	private final String avstemtReferanse;
	private final String avstemtDato;
	private final String brevProduksjonApplikasjon;
}
