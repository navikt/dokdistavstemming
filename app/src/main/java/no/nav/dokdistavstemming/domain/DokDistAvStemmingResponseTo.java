package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
	private DistribusjonKanalCode distribusjonKanal;
	private String distribusjonStatus;
	private LocalDateTime produksjonDato;
	private LocalDateTime distribusjonDato;
	private Long countDokument;
}
