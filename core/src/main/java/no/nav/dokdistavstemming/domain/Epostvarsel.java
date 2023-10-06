package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Epostvarsel {
	private final String adresse;
	private final String tittel;
	private final String tekst;
	private final String tidspunkt;
}
