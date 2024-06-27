package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokdistadmin.Rdist001administrerforsendelseConsumer;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.FeilregistrerForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTo;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTos;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelseRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.DokarkivConsumer;
import no.nav.dokdistavstemming.consumer.journalpostapi.OppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.exceptions.DokdistavstemmingFunctionalException;
import no.nav.doknotifikasjon.schemas.DoknotifikasjonStopp;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.partition;
import static java.time.LocalDateTime.now;
import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_BATCh_ID;
import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_CALL_ID;
import static no.nav.dokdistavstemming.consumer.dokdistadmin.Rdist001administrerforsendelseConsumer.HENTFORSENDELSER_MAX_JOURNALPOSTS;
import static no.nav.dokdistavstemming.domain.enums.UtsendingsKanalCode.NAV_NO;
import static no.nav.dokdistavstemming.utils.OpprettForsendelseMapper.mapForsendelseToTilOpprettForsendelse;
import static no.nav.dokdistavstemming.utils.Sdist006utils.DOKDISTDITTNAV;
import static no.nav.dokdistavstemming.utils.Sdist006utils.determineEkspedertTil;

@Slf4j
@Component
public class SendUlesteForsendelserTilSentralPrintService {
	private static final int ANTALL_DAGER_TILBAKE_MAX = 13;
	private static final int ANTALL_TIMER_TILBAKE_MIN = 40;

	private final DokarkivConsumer dokarkivConsumer;
	private final Rdist001administrerforsendelseConsumer rdist001administrerforsendelseConsumer;
	private final DistribuerTilSentralPrintMQService distribuerTilSentralPrintService;
	private final KafkaEventProducer kafkaEventProducer;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.HH.mm:ss");

	public SendUlesteForsendelserTilSentralPrintService(Rdist001administrerforsendelseConsumer rdist001administrerforsendelseConsumer,
														DokarkivConsumer dokarkivConsumer,
														DistribuerTilSentralPrintMQService distribuerTilSentralPrintService,
														KafkaEventProducer kafkaEventProducer) {
		this.dokarkivConsumer = dokarkivConsumer;
		this.rdist001administrerforsendelseConsumer = rdist001administrerforsendelseConsumer;
		this.distribuerTilSentralPrintService = distribuerTilSentralPrintService;
		this.kafkaEventProducer = kafkaEventProducer;
	}

	public void sendUlesteForsendelserTilSentralPrint() {
		try {
			MDC.put(MDC_BATCh_ID, LocalDateTime.now().format(formatter));
			log.info("Starter sdist006 cron-jobb");

			//1. Finn journalposter
			List<String> ulesteJournalposter = finnUlesteJournalposter();
			if (ulesteJournalposter == null || ulesteJournalposter.isEmpty()) {
				log.info("Sdist006 fant ingen uleste journalposter i Joark. Avslutter sdist006 cron-jobb.");
				return;
			}

			log.info("Sdist006 fant antall={} uleste journalposter i Joark", ulesteJournalposter.size());

			partition(ulesteJournalposter, HENTFORSENDELSER_MAX_JOURNALPOSTS).forEach(this::handleUlesteJournalposterList);

			log.info("Avslutter sdist006 cron-jobb");
		} finally {
			MDC.clear();
		}
	}

	private void handleUlesteJournalposterList(List<String> ulesteJournalposter) {
		log.info("Journalposter Sdist006 ønsker å sende til print:{}", String.join(",", ulesteJournalposter));

		//2. Finn forsendelser
		Optional<ForsendelseTos> ulesteForsendelserOptional = hentForsendelser(ulesteJournalposter);
		if (ulesteForsendelserOptional.isEmpty() || ulesteForsendelserOptional.get().forsendelseListe().isEmpty()) {
			log.info("Sdist006 fant ingen forsendelser for partisjonen av uleste journalposter.");
			return;
		}

		List<ForsendelseTo> ulesteForsendelser = ulesteForsendelserOptional.get().forsendelseListe();
		log.info("Sdist006 fant antall={} forsendelser tilhørende partisjonen av uleste journalposter", ulesteForsendelser.size());
		log.info("Forsendelser Sdist006 ønsker å feilregistrere/sende på nytt:{}", String.join(",", ulesteForsendelser.stream().map(ForsendelseTo::getBestillingsId).toList()));

		//3. Behandle forsendelser
		feilregistrerForsendelserOgSendTilQdist009(ulesteForsendelser);
	}

	private void feilregistrerForsendelserOgSendTilQdist009(List<ForsendelseTo> ulesteForsendelser) {
		ulesteForsendelser.forEach(gammelForsendelse -> {
			String gammelDistribusjonId = gammelForsendelse.getBestillingsId();
			String journalpostId = gammelForsendelse.getArkivInformasjon().getArkivId();
			String nyBestillingsId = UUID.randomUUID().toString();
			MDC.put(MDC_CALL_ID, gammelDistribusjonId);
			log.info("Sdist006 behandler ulest forsendelse med bestillingsId/distribusjonsId={} som ikke har blitt lest etter 40 timer",
					gammelDistribusjonId);
			try {
				// 3.1 Opprett ny forsendelse
				long nyForsendelsesId = opprettForsendelse(gammelForsendelse, nyBestillingsId);
				log.info("Sdist006 opprettet ny forsendelse med forsendelsesId={} for forsendelse med bestillingsId={}", nyForsendelsesId, gammelDistribusjonId);

				// 3.2 Feilregistrer original forsendelse
				feilregistrerForsendelse(gammelForsendelse.getForsendelseId(), nyBestillingsId);

				// 3.3 Sett status på ny forsendelse
				oppdaterForsendelse(nyForsendelsesId);

				// 3.4 Oppdater journalpost
				oppdaterJournalpost(journalpostId);

				// 3.5 Distribuer ny forsendelse
				distribuerTilSentralPrintService.sendToQdist009(nyForsendelsesId);

				// 3.6 stopp renotifikasjon av digital distribusjon
				stoppRenotifikasjon(gammelForsendelse.getBestillingsId());

				log.info("Sdist006 har håndtert: journalpostId={}, gammelDistribusjonsId={}, gammelForsendelseId={}, nyBestillingsId={}, nyForsendelseId={}",
						journalpostId, gammelDistribusjonId, gammelForsendelse.getForsendelseId(), nyBestillingsId, nyForsendelsesId);
			} catch (DokdistavstemmingFunctionalException e) {
				log.error("Sdist006 feilet under håndteringen av journalpostId={}, gammelDistribusjonsId={}, gammelForsendelseId={}, nyBestillingsId={}. Feilmelding:{}",
						journalpostId, gammelDistribusjonId, gammelForsendelse.getForsendelseId(), nyBestillingsId, e.getMessage());
			} finally {
				MDC.clear();
			}
		});
	}

	private List<String> finnUlesteJournalposter() {
		return dokarkivConsumer.finnUlesteJournalposter(NAV_NO, now().minusDays(ANTALL_DAGER_TILBAKE_MAX), determineEkspedertTil(now().minusHours(ANTALL_TIMER_TILBAKE_MIN)));
	}

	private Optional<ForsendelseTos> hentForsendelser(List<String> ulesteJournalposter) {
		return rdist001administrerforsendelseConsumer.hentForsendelser(ulesteJournalposter);
	}

	private Long opprettForsendelse(ForsendelseTo oldForsendelse, String nyBestillingsId) {
		return rdist001administrerforsendelseConsumer
				.opprettForsendelse(mapForsendelseToTilOpprettForsendelse(oldForsendelse, nyBestillingsId))
				.getForsendelseId();
	}

	private void feilregistrerForsendelse(long gammelForsendelseId, String nyBestillingsId) {
		FeilregistrerForsendelseRequest feilregistrerForsendelseRequest = FeilregistrerForsendelseRequest.builder()
				.forsendelseId(gammelForsendelseId)
				.feilTypeCode("MELDINGSFEIL")
				.tidspunkt(now())
				.detaljer("Forsendelse til NAV.NO er ikke lest innen frist.")
				.resendingDistribusjonId(nyBestillingsId)
				.build();
		rdist001administrerforsendelseConsumer.feilregistrerForsendelse(feilregistrerForsendelseRequest);
	}

	private void oppdaterForsendelse(Long nyForsendelsesId) {
		OppdaterForsendelseRequest oppdaterForsendelseRequest = OppdaterForsendelseRequest.builder()
				.forsendelseId(nyForsendelsesId)
				.forsendelseStatus("KLAR_FOR_DIST")
				.build();
		rdist001administrerforsendelseConsumer.oppdaterForsendelse(oppdaterForsendelseRequest);
	}

	private void oppdaterJournalpost(String journalpostId) {
		OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest = OppdaterDistribusjonsinfoRequest.builder()
				.settStatusEkspedert(false)
				.utsendingsKanal("S")
				.tilbakestillJournalpost(true)
				.build();
		dokarkivConsumer.oppdaterDistribusjonsinfo(oppdaterDistribusjonsinfoRequest, journalpostId);
	}

	private void stoppRenotifikasjon(String bestillingsId) {
		kafkaEventProducer.publish(new DoknotifikasjonStopp(bestillingsId, DOKDISTDITTNAV));
	}

}

