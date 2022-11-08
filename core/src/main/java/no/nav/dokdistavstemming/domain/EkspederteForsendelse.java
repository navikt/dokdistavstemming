package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class EkspederteForsendelse {
	private final Long forsendelseId;
	private final String journalpostId;
	private final String distribusjonsKanal;
	private final String ekspedertDato;
	private final PostadresseTo postadresse;
	private final Digitalpostkasse digitalpostkasse;
	private final DittNavVarsel varsel;
}
