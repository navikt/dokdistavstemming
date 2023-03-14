package no.nav.dokdistavstemming.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EkspedertForsendelse {
	private final Long forsendelseId;
	private final String journalpostId;
	private final String distribusjonsKanal;
	private final String ekspedertDato;
	private final PostadresseTo postadresse;
	private final Digitalpostkasse digitalpostkasse;
	private final Varsel varsel;
}
