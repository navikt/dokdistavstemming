package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import no.nav.dokdistavstemming.domain.DokDistAvstemmingForsendelse;

import java.util.List;

public interface HentUekspederKvitteringForsendelse {

	List<DokDistAvstemmingForsendelse> hentUekspederKvitteringForsendelse(String distribusjonKanal, Long antallTimer);

}
