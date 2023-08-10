package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import lombok.Builder;
import lombok.Data;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.enums.DistribusjonsTypeKode;
import no.nav.dokdistavstemming.domain.enums.DokumentStatusCode;

import java.util.List;

@Data
@Builder
public class HentForsendelseRequest {
	private List<DistribusjonsTypeKode> distribusjonstyper;
	private List<DokumentStatusCode> dokumentstatus;
	private DistribusjonKanalCode distribusjonkanal;
	private String[] journalpostliste;
}
