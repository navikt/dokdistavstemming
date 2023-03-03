package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Varsel {
	private final List<EpostVarsel> epostvarsel;
	private final List<SmsVarsel> smsvarsel;
}
