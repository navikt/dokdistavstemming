package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EkspedertForsendelse {
	private final Long forsendelseId;
	private final String journalpostId;
	private final String distribusjonsKanal;
	private final LocalDateTime ekspedertDato;
	private final PostadresseTo postadresse;
	private final Digitalpostkasse digitalpostkasse;
	private final Varsel varsel;
}
