package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import no.nav.dokdistavstemming.domain.DokDistAvstemmingForsendelse;

import java.util.List;

public interface HentUekspederForsendelse {

	List<DokDistAvstemmingForsendelse> hentUekspederForsendelse(String distribusjonKanal, Long antallTimer);

}
