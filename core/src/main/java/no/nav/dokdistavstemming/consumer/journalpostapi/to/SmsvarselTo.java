package no.nav.dokdistavstemming.consumer.journalpostapi.to;

import no.nav.dokdistavstemming.domain.Smsvarsel;

import java.time.LocalDateTime;

public record SmsvarselTo(String tekst, String mobilnummer, LocalDateTime varslingstidspunkt) {

	public static SmsvarselTo fromSmsvarsel(Smsvarsel varsel) {
		return new SmsvarselTo(varsel.getTekst(), varsel.getTelefonnummer(), varsel.getTidspunkt());
	}
}
