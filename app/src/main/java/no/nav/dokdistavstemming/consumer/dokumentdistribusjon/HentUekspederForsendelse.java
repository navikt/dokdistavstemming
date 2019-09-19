package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;

import java.util.List;

public interface HentUekspederForsendelse {

	List<HentUekspederForsendelseResponseTo> hentUekspederForsendelse(String distribusjonKanal, Long antallTimer);

}
