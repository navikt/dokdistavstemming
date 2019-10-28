package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import no.nav.dokdistavstemming.domain.DokDistAvstemmingRequestTo;

import java.util.List;

public interface HentForsendelseKvitteringIkkeMottatt {

	List<DokDistAvstemmingRequestTo> hentForsendelserKvitteringIkkeMottatt(String distribusjonKanal, Long antallTimer);

}
