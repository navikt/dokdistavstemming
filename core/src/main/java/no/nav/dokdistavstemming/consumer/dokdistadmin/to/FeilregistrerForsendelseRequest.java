package no.nav.dokdistavstemming.consumer.dokdistadmin.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeilregistrerForsendelseRequest {

	private Long forsendelseId;
	private String feilTypeCode;
	private LocalDateTime tidspunkt;
	private String detaljer;
	private String resendingDistribusjonId;
}
