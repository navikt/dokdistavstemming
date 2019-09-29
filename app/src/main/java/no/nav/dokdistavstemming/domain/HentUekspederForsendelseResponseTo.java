package no.nav.dokdistavstemming.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokdistavstemming.utils.CustomDeserializer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreType
public class HentUekspederForsendelseResponseTo {

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
