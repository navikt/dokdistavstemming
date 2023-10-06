package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Smsvarsel {
	private final String telefonnummer;
	private final String tekst;
	private final String tidspunkt;
}
