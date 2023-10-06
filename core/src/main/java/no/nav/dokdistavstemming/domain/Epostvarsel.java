package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Epostvarsel {
	private final String adresse;
	private final String tittel;
	private final String tekst;
	private final LocalDateTime tidspunkt;
}
