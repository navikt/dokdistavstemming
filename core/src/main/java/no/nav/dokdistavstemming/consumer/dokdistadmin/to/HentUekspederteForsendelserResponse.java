package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HentUekspederteForsendelserResponse {

	private List<UekspedertForsendelse> uekspederteForsendelser;

	@Data
	@Builder
	public static class UekspedertForsendelse {
		private String distribusjonId;
		private List<DokumentInfo> dokumenter;
		private String distribusjonKanal;
		private String distribusjonStatus;
		private String opprettetDato;
		private String distribusjonDato;
	}

	@Data
	@Builder
	public static class DokumentInfo {
		private final String forsendelseId;
		private final String dokumentId;
		private final String dokumentStatus;
		private final String konversasjonId;
		private final String bestillendeFagsystem;
		private final String fagomradeCode;
		private final String journalpostId;
		private final String brevProduksjonApplikasjon;
	}

	public static HentUekspederteForsendelserResponse empty() {
		return new HentUekspederteForsendelserResponse(List.of());
	}

}
