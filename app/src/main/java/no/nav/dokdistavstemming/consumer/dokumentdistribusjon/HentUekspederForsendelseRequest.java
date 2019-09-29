package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HentUekspederForsendelseRequest {

	private String distribusjonKanal;
	private Long antallTimer;
}
