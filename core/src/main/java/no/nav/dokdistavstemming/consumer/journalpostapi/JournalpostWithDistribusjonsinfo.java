package no.nav.dokdistavstemming.consumer.journalpostapi;


import lombok.Builder;
import lombok.Data;
import no.nav.dokdistavstemming.domain.Digitalpostkasse;
import no.nav.dokdistavstemming.domain.Varsel;
import no.nav.dokdistavstemming.domain.PostadresseTo;

import java.time.OffsetDateTime;

@Data
@Builder
public class JournalpostWithDistribusjonsinfo {
	private Boolean settStatusEkspedert;
	private String utsendingsKanal;
	private Long forsendelseId;
	private Long journalpostId;
	private OffsetDateTime ekspedertDato;
	private PostadresseTo postadresse;
	private Digitalpostkasse digitalpostkasse;
	private Varsel varsel;
}
