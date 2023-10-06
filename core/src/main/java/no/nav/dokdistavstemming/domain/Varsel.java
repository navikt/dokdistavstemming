package no.nav.dokdistavstemming.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Varsel {
	private final List<Epostvarsel> epostvarsel;
	private final List<Smsvarsel> smsvarsel;
}
