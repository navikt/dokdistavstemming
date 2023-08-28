package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokdistadmin.Rdist001administrerforsendelseConsumer;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.FeilregistrerForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTo;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTos;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentForsendelseRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelseRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.DokarkivConsumer;
import no.nav.dokdistavstemming.consumer.journalpostapi.OppdaterDistribusjonsinfoRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.DITTNAV;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonsTypeKode.VEDTAK;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonsTypeKode.VIKTIG;
import static no.nav.dokdistavstemming.domain.enums.DokumentStatusCode.EKSPEDERT;
import static no.nav.dokdistavstemming.domain.enums.UtsendingsKanalCode.NAV_NO;
import static no.nav.dokdistavstemming.utils.DateUtils.determineEkspedertTil;
import static no.nav.dokdistavstemming.utils.OpprettForsendelseMapper.mapForsendelseToTilOpprettForsendelse;

@Slf4j
@Component
public class SendUlesteForsendelserTilSentralPrintService {

	private final DokarkivConsumer dokarkivConsumer;
	private final Rdist001administrerforsendelseConsumer rdist001administrerforsendelseConsumer;
	private final DistribuerTilSentralPrintMQService distribuerTilSentralPrintService;

	public SendUlesteForsendelserTilSentralPrintService(Rdist001administrerforsendelseConsumer rdist001administrerforsendelseConsumer, DokarkivConsumer dokarkivConsumer, DistribuerTilSentralPrintMQService distribuerTilSentralPrintService) {
		this.dokarkivConsumer = dokarkivConsumer;
		this.rdist001administrerforsendelseConsumer = rdist001administrerforsendelseConsumer;
		this.distribuerTilSentralPrintService = distribuerTilSentralPrintService;
	}

	public void sendUlesteForsendelserTilSentralPrint() {
		//1. Finn journalposter
		List<String> ulesteJournalposter = finnUlesteJournalposter();
		if (ulesteJournalposter == null || ulesteJournalposter.size() == 0) {
			log.info("Sdist006 fant ingen uleste journalposter i Joark.");
			return;
		}
		log.info("Sdist006 fant antall={} uleste journalposter i Joark", ulesteJournalposter.size());

		//2. Finn forsendelser
		Optional<ForsendelseTos> ulesteForsendelserOptional = hentForsendelser(ulesteJournalposter);
		if (ulesteForsendelserOptional.isEmpty() || ulesteForsendelserOptional.get().forsendelseListe().isEmpty()) {
			log.info("Sdist006 fant ingen uleste forsendelser for de uleste journalpostene.");
			return;
		}
		List<ForsendelseTo> ulesteForsendelser = ulesteForsendelserOptional.get().forsendelseListe();
		log.info("Sdist006 fant {} forsendelser tilhørende de uleste journalpostene", ulesteForsendelser.size());

		log.info("Journalposter Sdist006 ønsker å sende til print:{}", String.join(",", ulesteJournalposter));
		log.info("Forsendelser Sdist006 ønsker å feilregistrere:{}", String.join(",", ulesteForsendelser.stream().map(ForsendelseTo::getBestillingsId).toList()));
		MDC.clear();
/*
		//3. Behandle forsendelser
		ulesteForsendelser.forEach(forsendelseTo -> {
			try {
				String gammelBestillingsId = forsendelseTo.getBestillingsId();
				log.info("Sdist006 behandler ulest forsendelse med bestillingsId={} som ikke har blitt lest etter 40 timer",
						gammelBestillingsId);
				MDC.put(MDC_CALL_ID, gammelBestillingsId);
				String journalpostId = forsendelseTo.getArkivInformasjon().getArkivId();

				//3.1 Opprett ny forsendelse
				long nyForsendelsesId = opprettForsendelse(forsendelseTo);

				//3.2 Feilregistrer original forsendelse
				feilregistrerForsendelse(gammelBestillingsId);

				// 3.3 Sett status på ny forsendelse
				oppdaterForsendelse(nyForsendelsesId);

				//3.4 Oppdater journalpost
				oppdaterJournalpost(journalpostId);

				//3.5 Distribuer ny forsendelse
				distribuerTilSentralPrintService.sendToQdist009(nyForsendelsesId);
			} finally {
				MDC.clear();
			}
		});*/
	}

	private List<String> finnUlesteJournalposter() {
		LocalDateTime ulesteJournalposterEkspedertTil = LocalDateTime.now().minusHours(40);
		return dokarkivConsumer.finnUlesteJournalposter(NAV_NO, LocalDateTime.now().minusDays(7), determineEkspedertTil(ulesteJournalposterEkspedertTil));
	}

	private Optional<ForsendelseTos> hentForsendelser(List<String> ulesteJournalposter) {
		HentForsendelseRequest hentForsendelseRequest = HentForsendelseRequest.builder()
				.distribusjonstyper(List.of(VIKTIG, VEDTAK))
				.dokumentstatus(singletonList(EKSPEDERT))
				.distribusjonkanal(DITTNAV)
				.journalpostliste(ulesteJournalposter)
				.build();
		return rdist001administrerforsendelseConsumer.hentForsendelser(hentForsendelseRequest);
	}

	private Long opprettForsendelse(ForsendelseTo oldForsendelse) {
		ForsendelseTo opprettForsendelseRequest = mapForsendelseToTilOpprettForsendelse(oldForsendelse, UUID.randomUUID().toString());
		return rdist001administrerforsendelseConsumer.opprettForsendelse(opprettForsendelseRequest).getForsendelseId();
	}

	private void feilregistrerForsendelse(String gammelBestillingsId) {
		FeilregistrerForsendelseRequest feilregistrerForsendelseRequest = FeilregistrerForsendelseRequest.builder()
				.feilTypeCode("MELDINGSFEIL")
				.tidspunkt(LocalDateTime.now())
				.detaljer("Forsendelse til NAV.NO er ikke lest innen frist.")
				.resendingDistribusjonId(gammelBestillingsId)
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

}

