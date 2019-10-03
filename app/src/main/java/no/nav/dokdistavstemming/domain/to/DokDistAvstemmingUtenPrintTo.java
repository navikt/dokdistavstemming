package no.nav.dokdistavstemming.domain.to;

import lombok.Builder;
import lombok.Data;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Data
@Builder
public class DokDistAvstemmingUtenPrintTo {

	private String forsendelseId;
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
