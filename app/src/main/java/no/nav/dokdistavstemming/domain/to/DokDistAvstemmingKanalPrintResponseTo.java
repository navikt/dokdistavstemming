package no.nav.dokdistavstemming.domain.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;



@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DokDistAvstemmingKanalPrintResponseTo {

	private String forsendelseId;
	private String distribusjonKanal;
	private String distribusjonStatus;
	private String dokumentStatus;
	private String produksjonDato;
	private String distribusjonDato;
	private Long countDokument;
}
