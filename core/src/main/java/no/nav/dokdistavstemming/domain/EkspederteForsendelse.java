package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EkspederteForsendelse {
	private final String forsendelseId;
	private final String journalpostId;
	private final DistribusjonKanalCode distribusjonsKanal;
	private final LocalDateTime ekspedertDato;
	private final PostadresseTo postadresse;
	private final Digitalpostkasse digitalpostkasse;
	private final DittNavVarsel varsel;
}
