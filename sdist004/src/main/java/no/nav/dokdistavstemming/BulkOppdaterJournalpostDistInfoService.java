package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokdistadmin.DokdistadminRdist001Api;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.DokarkivConsumer;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResultResponse;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.DITTNAV;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.DPVT;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.E_HANDEL;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.TRYGDERETTEN;

@Slf4j
@Component
public class BulkOppdaterJournalpostDistInfoService {

	private static final int MAX_SIZE = 1000;
	private final DokarkivConsumer dokarkivConsumer;
	private final DokdistadminRdist001Api dokdistadminRdist001Api;
	private final BulkOppdaterDistribusjonsinfoMapper bulkOppdaterDistribusjonsinfoMapper;
	private final AvstemEkspederteForsendelserMapper avstemEkspederteForsendelserMapper;

	public BulkOppdaterJournalpostDistInfoService(DokarkivConsumer dokarkivConsumer,
												  DokdistadminRdist001Api dokdistadminRdist001Api) {
		this.dokarkivConsumer = dokarkivConsumer;
		this.dokdistadminRdist001Api = dokdistadminRdist001Api;
		this.bulkOppdaterDistribusjonsinfoMapper = new BulkOppdaterDistribusjonsinfoMapper();
		this.avstemEkspederteForsendelserMapper = new AvstemEkspederteForsendelserMapper();
	}

	public void oppdaterAvstemOgJournalpostDistInfo() {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserResponse = dokdistadminRdist001Api.hentEkspederteforsendelser();

		if (hentEkspederteForsendelserResponse.getForsendelser().isEmpty()) {
			log.info("Sdist004 fant ingen ekspederte forsendelser i dokdist-db. Avslutter sdist004 cron-jobb.");
			return;
		}

		if (!isForsendelseNullOrEmpy(hentEkspederteForsendelserResponse)) {

			countByDistribusjonKanal(hentEkspederteForsendelserResponse);
			BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = bulkOppdaterDistribusjonsinfoMapper.map(hentEkspederteForsendelserResponse);

			nPartitionJournalpost(bulkOppdaterDistribusjonsinfoRequest).forEach(jpRequest -> {

				BulkOppdaterDistribusjonsinfoResponse bulkOppdaterDistribusjonsinfoResponse = bulkOppdaterDistribusjonsinfoRequest == null ? null :
						dokarkivConsumer.bulkOppdaterJournalpostDistribusjonsInfo(jpRequest);
				logMelding(bulkOppdaterDistribusjonsinfoResponse);

				AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest =
						avstemEkspederteForsendelserMapper.mapAvstemEkspederteForsendelser(hentEkspederteForsendelserResponse, bulkOppdaterDistribusjonsinfoResponse.getJournalposter());

				if (avstemEkspederteForsendelserRequest != null) {
					log.info("Sdist004 oppdaterte {} journalposter med distribusjonsinformasjon på dokarkiv, og feilet totalt på {} journalposter",
							countSuccess(bulkOppdaterDistribusjonsinfoResponse.getJournalposter()), countFeil(bulkOppdaterDistribusjonsinfoResponse.getJournalposter()));
					dokdistadminRdist001Api.oppdaterAvstemEkspederteForsendelser(avstemEkspederteForsendelserRequest);
				}

			});
		}

		log.info("Avslutter sdist004 cron-jobb");
	}

	private boolean isForsendelseNullOrEmpy(HentEkspederteForsendelserResponse hentEkspederteForsendelser) {
		return hentEkspederteForsendelser.getForsendelser() == null;
	}

	private List<BulkOppdaterDistribusjonsinfoRequest> nPartitionJournalpost(BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest) {
		AtomicInteger counter = new AtomicInteger();
		return bulkOppdaterDistribusjonsinfoRequest == null ? null :
				bulkOppdaterDistribusjonsinfoRequest.getJournalposter().stream()
						.collect(Collectors.groupingBy(i -> counter.getAndIncrement() / MAX_SIZE))
						.values().stream()
						.map(jp -> BulkOppdaterDistribusjonsinfoRequest.builder().journalposter(jp).build())
						.toList();
	}

	private void logMelding(BulkOppdaterDistribusjonsinfoResponse response) {
		if (response.getJournalposter() != null) {
			if (response.getJournalposter().getFeilet() != null) {
				List<JournalpostResponse> feil = response.getJournalposter().getFeilet();
				log.warn("Sdist004 feilet med å oppdatere {} journalposter på dokarkiv med feilmeldinger={}", countFeil(response.getJournalposter()), feil);
			}
		}
	}

	private int countFeil(JournalpostResultResponse jpResult) {
		if (jpResult != null) {
			return jpResult.getFeilet() == null ? 0 : jpResult.getFeilet().size();
		}
		return 0;
	}

	private int countSuccess(JournalpostResultResponse jpResult) {
		if (jpResult != null) {
			return jpResult.getOppdatert() == null ? 0 : jpResult.getOppdatert().size();
		}
		return 0;
	}

	private void countByDistribusjonKanal(HentEkspederteForsendelserResponse forsendelserResponse) {
		Map<DistribusjonKanalCode, Long> collectByKanal = forsendelserResponse.getForsendelser().stream()
				.map(forsendelse -> DistribusjonKanalCode.valueOf(forsendelse.getDistribusjonsKanal()))
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		log.info("Sdist004 hentet {} ekspederte forsendelser fra dokdistadmin fordelt på følgende kanaler: DPVT={}, E_HANDEL={}, DITTNAV={}, PRINT={}, SDP={}, TRYGDERETTEN={}",
				forsendelserResponse.getForsendelser().size(), collectByKanal.get(DPVT), collectByKanal.get(E_HANDEL), collectByKanal.get(DITTNAV), collectByKanal.get(PRINT), collectByKanal.get(SDP), collectByKanal.get(TRYGDERETTEN));
	}
}
