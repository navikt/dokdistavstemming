package no.nav.dokdistavstemming.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DokDistAvstemmingRequestTo {

	private String forsendelseId;
	private List<DokumentInfoTo> dokumenter;
	private String distribusjonKanal;
	private String distribusjonStatus;
	private String produksjonDato;
	private String distribusjonDato;
	private Long countDokument;


	@Data
	@Builder
	public static class DokumentInfoTo {
		private final String konversasjonId;
		private final String digitalDistributorId;
		private final String bestillendeFagsystem;
		private final String fagomradeCode;
		private final String dokumentStatus;
		private final String mottakkerId;
		private final String arkivKode;

	}


}
