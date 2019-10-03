package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import no.nav.dokdistavstemming.domain.DokDistAvstemmingRequestTo;

import java.util.List;

public interface HentUekspederForsendelse {

	List<DokDistAvstemmingRequestTo> hentUekspederForsendelse(String distribusjonKanal, Long antallTimer);

}
