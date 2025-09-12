package no.nav.dokdistavstemming.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

import static no.nav.dokdistavstemming.constants.DokdistavstemmingConstants.NAV_LOCAL_DATE_TIME_FORMAT;

@Builder
public record UekspedertForsendelseDokument(
		String distribusjonId,
		String distribusjonKanal,
		String distribusjonStatus,
		@JsonFormat(pattern = NAV_LOCAL_DATE_TIME_FORMAT)
		LocalDateTime opprettetDato,
		@JsonFormat(pattern = NAV_LOCAL_DATE_TIME_FORMAT)
		LocalDateTime distribusjonDato,
		Long forsendelseId,
		String dokumentId,
		String dokumentStatus,
		String konversasjonId,
		String bestillendeFagsystem,
		String fagomradeCode,
		Long journalpostId,
		String brevProduksjonApplikasjon
) {
}
