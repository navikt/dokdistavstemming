package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HentEkspederteForsendelserRequest {
	private int maksForsendelser;
}
