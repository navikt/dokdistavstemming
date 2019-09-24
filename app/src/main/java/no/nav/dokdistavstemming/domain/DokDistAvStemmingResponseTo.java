package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Data
@Builder
public class DokDistAvStemmingResponseTo {

	private String konversasjonId;
	private String digitalDistributorId;
	private String bestillendeFagsystem;
	private String fagomradeCode;
	private String dokumentStatus;
	private String mottakkerId;
	private String arkivKode;
	private String forsendelseId;
	private String distribusjonKanal;
	private String distribusjonStatus;
	private LocalDateTime produksjonDato;
	private LocalDateTime distribusjonDato;
	private Long countDokument;
}
