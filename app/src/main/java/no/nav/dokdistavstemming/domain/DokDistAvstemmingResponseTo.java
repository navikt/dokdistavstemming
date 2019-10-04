package no.nav.dokdistavstemming.domain;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Data
@Builder
@ToString
public class DokDistAvstemmingResponseTo {

	private String distribusjonId;
	private String konversasjonId;
	private String arkivKode;
	private String digitalDistributorId;
	private String bestillendeFagsystem;
	private String fagomradeCode;
	private String dokumentStatus;
	private String mottakkerId;
	private String distribusjonKanal;
	private String distribusjonStatus;
	private String produksjonDato;
	private String distribusjonDato;
	private Long countDokument;
}
