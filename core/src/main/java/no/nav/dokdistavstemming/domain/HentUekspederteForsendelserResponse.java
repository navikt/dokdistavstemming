package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HentUekspederteForsendelserResponse {

	private List<UekspedertForsendelse> uekspedertForsendelseList;

	@Data
	@Builder
	public static class UekspedertForsendelse {
		private String distribusjonId;
		private List<DokumentInfoTo> dokumenter;
		private String distribusjonKanal;
		private String distribusjonStatus;
		private String opprettetDato;
		private String distribusjonDato;
	}

	@Data
	@Builder
	public static class DokumentInfoTo {
		private final String forsendelseId;
		private final String dokumentId;
		private final String dokumentStatus;
		private final String konversasjonId;
		private final String bestillendeFagsystem;
		private final String fagomradeCode;
		private final String journalpostId;
		private final String brevProduksjonApplikasjon;
		private final String avstemtReferanse;
		private final String avstemtDato;
	}

}
