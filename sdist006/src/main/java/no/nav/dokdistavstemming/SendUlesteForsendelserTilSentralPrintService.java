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
import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_CALL_ID;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.DITTNAV;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonsTypeKode.VEDTAK;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonsTypeKode.VIKTIG;
import static no.nav.dokdistavstemming.domain.enums.DokumentStatusCode.EKSPEDERT;

@Slf4j
@Component
public class SendUlesteForsendelserTilSentralPrintService {

	private final DokarkivConsumer dokarkivConsumer;
	private final Rdist001administrerforsendelseConsumer rdist001administrerforsendelseConsumer;

	public SendUlesteForsendelserTilSentralPrintService(Rdist001administrerforsendelseConsumer rdist001administrerforsendelseConsumer, DokarkivConsumer dokarkivConsumer) {
		this.dokarkivConsumer = dokarkivConsumer;
		this.rdist001administrerforsendelseConsumer = rdist001administrerforsendelseConsumer;
	}

	public void sendUlesteForsendelserTilSentralPrint() {
		//1. Finn journalposter
		String[] ulesteJournalposter = finnUlesteJournalposter();
		if (ulesteJournalposter == null || ulesteJournalposter.length == 0) {
			log.info("Sdist006 fant ingen uleste journalposter i Joark.");
			return;
		}
		log.info("Sdist006 fant {} uleste journalposter i Joark", ulesteJournalposter.length);

		//2. Finn forsendelser
		Optional<ForsendelseTos> ulesteForsendelserOptional = hentForsendelser(ulesteJournalposter);
		if (ulesteForsendelserOptional.isEmpty() || ulesteForsendelserOptional.get().forsendelseListe().isEmpty()) {
			log.info("Sdist006 fant ingen uleste forsendelser for de uleste journalpostene.");
			return;
		}
		List<ForsendelseTo> ulesteForsendelser = ulesteForsendelserOptional.get().forsendelseListe();
		log.info("Sdist006 fant {} forsendelser tilhørende de uleste journalpostene", ulesteForsendelser.size());

		//3. Behandle forsendelser
		//Denne kan nok parallelliseres. Må sette meg litt mer inn i hvordan ThreadPoolTaskExecutor funker
		ulesteForsendelser.forEach(forsendelseTo -> {
			try {
				MDC.put(MDC_CALL_ID, UUID.randomUUID().toString());
				String gammelBestillingsId = forsendelseTo.getBestillingsId();
				String journalpostId = forsendelseTo.getArkivInformasjon().getArkivId();

				//3.1 Opprett ny forsendelse
				long nyForsendelsesId = opprettForsendelse(forsendelseTo, UUID.randomUUID().toString());

				//3.2 Feilregistrer original forsendelse
				feilregistrerForsendelse(gammelBestillingsId);

				// 3.3 Sett status på ny forsendelse
				oppdaterForsendelse(nyForsendelsesId);

				//3.4 Oppdater journalpost
				oppdaterJournalpost(journalpostId);

				//3.5 Distribuer ny forsendelse
				//TODO: Sett opp mq
			} finally {
				MDC.clear();
			}
		});
	}

	private String[] finnUlesteJournalposter() {
		return dokarkivConsumer.finnUlesteJournalposter(DITTNAV, LocalDateTime.now().minusDays(7), determineEkspedertTil());
	}

	private Optional<ForsendelseTos> hentForsendelser(String[] ulesteJournalposter) {
		HentForsendelseRequest hentForsendelseRequest = HentForsendelseRequest.builder()
				.distribusjonstyper(List.of(VIKTIG, VEDTAK))
				.dokumentstatus(singletonList(EKSPEDERT))
				.distribusjonkanal(DITTNAV)
				.journalpostliste(ulesteJournalposter)
				.build();
		return rdist001administrerforsendelseConsumer.hentForsendelser(hentForsendelseRequest);
	}

	private Long opprettForsendelse(ForsendelseTo forsendelseTo, String nyBestillingsId) {
		forsendelseTo.setOriginalDistribusjonId(forsendelseTo.getBestillingsId());
		forsendelseTo.setBestillingsId(nyBestillingsId);
		forsendelseTo.setDistribusjonsKanal(PRINT);
		forsendelseTo.getDokumenter().forEach(d -> d.setDokumenttypeId("U000001"));

		return rdist001administrerforsendelseConsumer.opprettForsendelse(forsendelseTo).getForsendelseId();
	}

	private void feilregistrerForsendelse(String forsendelsesId) {
		FeilregistrerForsendelseRequest feilregistrerForsendelseRequest = FeilregistrerForsendelseRequest.builder()
				.feilTypeCode("MELDINGSFEIL")
				.tidspunkt(LocalDateTime.now())
				.detaljer("Forsendelse til NAV.NO er ikke lest innen frist.")
				.resendingDistribusjonId(forsendelsesId)
				.build();
		rdist001administrerforsendelseConsumer.feilregistrerForsendelse(feilregistrerForsendelseRequest);
	}

	private void oppdaterForsendelse(Long forsendelsesId) {
		OppdaterForsendelseRequest oppdaterForsendelseRequest = OppdaterForsendelseRequest.builder()
				.forsendelseId(forsendelsesId)
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

	private LocalDateTime determineEkspedertTil(){
		LocalDateTime ekspedertTil = LocalDateTime.now().minusHours(40);
		return switch (ekspedertTil.getDayOfWeek()) {
			case SATURDAY -> setKlokkeslettTil16(ekspedertTil.minusDays(1));
			case SUNDAY -> setKlokkeslettTil16(ekspedertTil.minusDays(2));
			default -> ekspedertTil;
		};
	}

	private LocalDateTime setKlokkeslettTil16(LocalDateTime date) {
		return date.withHour(16).withMinute(0).withSecond(0);
	}
}

