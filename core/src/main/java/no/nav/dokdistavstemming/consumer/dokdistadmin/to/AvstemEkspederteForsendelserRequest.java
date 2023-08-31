package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokdistavstemming.domain.Forsendelse;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvstemEkspederteForsendelserRequest {

	private List<Forsendelse> forsendelser;
}