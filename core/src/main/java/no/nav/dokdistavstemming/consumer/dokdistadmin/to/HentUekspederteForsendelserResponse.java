package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.dokdistavstemming.constants.DokdistavstemmingConstants.NAV_LOCAL_DATE_TIME_FORMAT;

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
		@JsonFormat(pattern = NAV_LOCAL_DATE_TIME_FORMAT)
		private LocalDateTime opprettetDato;
		@JsonFormat(pattern = NAV_LOCAL_DATE_TIME_FORMAT)
		private LocalDateTime distribusjonDato;
	}

	@Data
	@Builder
	public static class DokumentInfo {
		private final Long forsendelseId;
		private final String dokumentId;
		private final String dokumentStatus;
		private final String konversasjonId;
		private final String bestillendeFagsystem;
		private final String fagomradeCode;
		private final Long journalpostId;
		private final String brevProduksjonApplikasjon;
	}

	public static HentUekspederteForsendelserResponse empty() {
		return new HentUekspederteForsendelserResponse(List.of());
	}

}
