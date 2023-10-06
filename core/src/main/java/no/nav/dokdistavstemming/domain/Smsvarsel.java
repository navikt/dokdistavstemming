package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Smsvarsel {
	private final String telefonnummer;
	private final String tekst;
	private final LocalDateTime tidspunkt;
}
