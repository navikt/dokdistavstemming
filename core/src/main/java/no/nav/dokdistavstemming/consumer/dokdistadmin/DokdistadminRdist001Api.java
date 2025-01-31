package no.nav.dokdistavstemming.consumer.dokdistadmin;


import no.nav.dokdistavstemming.consumer.dokdistadmin.to.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.FeilregistrerForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTo;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTos;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.Forsendelse;

import java.util.List;
import java.util.Optional;

public interface DokdistadminRdist001Api {

	HentUekspederteForsendelserResponse hentForsendelserKvitteringIkkeMottatt(String distribusjonKanal, int antallTimer);

	void oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo);

	void oppdaterAvstemEkspederteForsendelser(AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest);

	HentEkspederteForsendelserResponse hentEkspederteforsendelser();

	Optional<ForsendelseTos> hentForsendelser(List<String> journalpostList);

	Forsendelse opprettForsendelse(ForsendelseTo forsendelseTo);

	void feilregistrerForsendelse(FeilregistrerForsendelseRequest feilregistrerForsendelseRequest);

	void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelseRequest);
}
