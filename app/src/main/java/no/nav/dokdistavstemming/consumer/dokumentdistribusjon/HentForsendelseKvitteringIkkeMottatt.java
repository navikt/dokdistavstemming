package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;

import java.util.List;

public interface HentForsendelseKvitteringIkkeMottatt {

	List<AvstemForsendelseRequestTo> hentForsendelserKvitteringIkkeMottatt(String distribusjonKanal, Long antallTimer);

}
