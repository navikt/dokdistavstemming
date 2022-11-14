package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DittNavVarsel {
	private final String digitalkontaktinformasjon;
	private final String varseltekst;
}
