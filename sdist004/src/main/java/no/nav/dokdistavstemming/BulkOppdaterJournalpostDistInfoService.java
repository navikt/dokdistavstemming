package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.Rdist001administrerforsendelse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterJournalpostDistInfoConsumer;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResultResponse;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.DITTNAV;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.E_HANDEL;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.TRYGDERETTEN;

@Slf4j
@Component
public class BulkOppdaterJournalpostDistInfoService {

	private final BulkOppdaterJournalpostDistInfoConsumer oppdaterJournalpostDistInfoConsumer;
	private final Rdist001administrerforsendelse rdist001administrerforsendelse;
	private final BulkOppdaterDistribusjonsinfoMapper bulkOppdaterDistribusjonsinfoMapper;
	private final AvstemEkspederteForsendelserMapper avstemEkspederteForsendelserMapper;
	private static final int MAX_SIZE = 1000;

	public BulkOppdaterJournalpostDistInfoService(BulkOppdaterJournalpostDistInfoConsumer oppdaterJournalpostDistInfoConsumer,
												  Rdist001administrerforsendelse rdist001administrerforsendelse) {
		this.oppdaterJournalpostDistInfoConsumer = oppdaterJournalpostDistInfoConsumer;
		this.rdist001administrerforsendelse = rdist001administrerforsendelse;
		this.bulkOppdaterDistribusjonsinfoMapper = new BulkOppdaterDistribusjonsinfoMapper();
		this.avstemEkspederteForsendelserMapper = new AvstemEkspederteForsendelserMapper();
	}

	public void oppdaterAvstemOgJournalpostDistInfo() {
		HentEkspederteForsendelserResponse hentEkspederteForsendelserResponse = rdist001administrerforsendelse.hentEkspederteforsendelser();

		if (hentEkspederteForsendelserResponse == null) {
			log.info("Fant ikke ekspederteforsendelse i dokdist-db.");
			return;
		}

		if (!isForsendelseNullOrEmpy(hentEkspederteForsendelserResponse)) {

			countByDistribusjonKanal(hentEkspederteForsendelserResponse);
			BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = bulkOppdaterDistribusjonsinfoMapper.map(hentEkspederteForsendelserResponse);

			nPartitionJournalpost(bulkOppdaterDistribusjonsinfoRequest).forEach(jpRequest -> {

				BulkOppdaterDistribusjonsinfoResponse bulkOppdaterDistribusjonsinfoResponse = bulkOppdaterDistribusjonsinfoRequest == null ? null :
						oppdaterJournalpostDistInfoConsumer.bulkOppdaterJournalpostDistribusjonsInfo(jpRequest);
				logMelding(bulkOppdaterDistribusjonsinfoResponse);

				AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest = bulkOppdaterDistribusjonsinfoResponse == null ? null :
						avstemEkspederteForsendelserMapper.mapAvstemEkspederteForsendelser(hentEkspederteForsendelserResponse, bulkOppdaterDistribusjonsinfoResponse.getJournalposter());

				if (avstemEkspederteForsendelserRequest != null) {
					log.info("sdist004 oppdaterte totalt={} journalposter distribusjonsinformasjon på dokarkiv og feilet totalt={}",
							countSuccess(bulkOppdaterDistribusjonsinfoResponse.getJournalposter()), countFeil(bulkOppdaterDistribusjonsinfoResponse.getJournalposter()));
					rdist001administrerforsendelse.oppdaterAvstemEkspederteForsendelser(avstemEkspederteForsendelserRequest);
				}

			});
		}
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
				log.warn("sdist004 feilet til å oppdatere totalt={} journalposter på dokarkiv. {}", countFeil(response.getJournalposter()), feil);
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
		log.info("sdist004 hentet totalt:[E_HANDEL:{}, DITTNAV={}, PRINT={}, SDP={}, TRYGDERETTEN={}]={} ekspederteforsendelse fra dokdist-rdist001.", collectByKanal.get(E_HANDEL), collectByKanal.get(DITTNAV), collectByKanal.get(PRINT),
				collectByKanal.get(SDP), collectByKanal.get(TRYGDERETTEN), forsendelserResponse.getForsendelser().size());
	}
}
