package no.nav.dokdistavstemming.consumer.dokdistadmin;


import no.nav.dokdistavstemming.domain.Forsendelse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.FeilregistrerForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTo;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTos;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.exceptions.DokdistadminTechnicalException;
import no.nav.dokdistavstemming.metrics.Monitor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.Optional;

import static no.nav.dokdistavstemming.constants.MDCConstants.DOK_REQUEST;
import static no.nav.dokdistavstemming.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokdistavstemming.constants.RetryConstants.MULTIPLIER_SHORT;

public interface Rdist001administrerforsendelse {

	HentUekspederteForsendelserResponse hentForsendelserKvitteringIkkeMottatt(String distribusjonKanal, int antallTimer);

	void oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo);

	void oppdaterAvstemEkspederteForsendelser(AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest);

	HentEkspederteForsendelserResponse hentEkspederteforsendelser();

	Optional<ForsendelseTos> hentForsendelser(HentForsendelseRequest hentForsendelseRequest);

	Forsendelse opprettForsendelse(ForsendelseTo forsendelseTo);

	void feilregistrerForsendelse(FeilregistrerForsendelseRequest feilregistrerForsendelseRequest);

	void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelseRequest);
}
