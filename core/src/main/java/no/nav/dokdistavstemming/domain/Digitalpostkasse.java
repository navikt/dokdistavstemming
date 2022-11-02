package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Digitalpostkasse {
	private final String digitalpostkasseadresse;
	private final String digitalpostkasseleverandor;
}
