package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HentEkspederteForsendelserResponse {
	private final List<EkspederteForsendelse> forsendelser;
}
