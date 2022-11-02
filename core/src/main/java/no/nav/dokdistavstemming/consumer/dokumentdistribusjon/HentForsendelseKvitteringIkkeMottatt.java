package no.nav.dokdistavstemming.consumer.dokumentdistribusjon;


import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.domain.OppdaterForsendelserAvstemtInfo;

import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */
public interface HentForsendelseKvitteringIkkeMottatt {

	List<AvstemForsendelseRequestTo> hentForsendelserKvitteringIkkeMottatt(String distribusjonKanal, int antallTimer);

	void oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo);

	void oppdaterAvstemEkspederteForsendelser(AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest);

	HentEkspederteForsendelserResponse hentEkspederteforsendelser(HentEkspederteForsendelserRequest hentEkspederteForsendelserRequest);
}
