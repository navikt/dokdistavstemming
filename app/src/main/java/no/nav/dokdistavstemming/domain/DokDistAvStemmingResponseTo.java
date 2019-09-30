package no.nav.dokdistavstemming.domain;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import no.nav.dokdistavstemming.utils.LocalDateSerializer;

import java.time.LocalDateTime;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Data
@Builder
@ToString
public class DokDistAvStemmingResponseTo {

	private String konversasjonId;
	private String digitalDistributorId;
	private String bestillendeFagsystem;
	private String fagomradeCode;
	private String dokumentStatus;
	private String mottakkerId;
	private String arkivKode;
	private String forsendelseId;
	private DistribusjonKanalCode distribusjonKanal;
	private String distribusjonStatus;
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDateTime produksjonDato;
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDateTime distribusjonDato;
	private Long countDokument;
}
