package no.nav.dokdistavstemming.consumer.journalpostapi.to;

import no.nav.dokdistavstemming.domain.Epostvarsel;

import java.time.LocalDateTime;

public record EpostvarselTo(String tittel, String tekst, String epostadresse, LocalDateTime varslingstidspunkt) {

	public static EpostvarselTo fromEpostvarsel(Epostvarsel varsel) {
		return new EpostvarselTo(varsel.getTittel(), varsel.getTekst(), varsel.getAdresse(), varsel.getTidspunkt());
	}
}
