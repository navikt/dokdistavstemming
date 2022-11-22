package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.Rdist001administrerforsendelse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterJournalpostDistInfoConsumer;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostResultResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostWithDistribusjonsinfo;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
			log.info("sdist004 hentet totalt {} ekspederteforsendelse fra dokdist-rdist001.", hentEkspederteForsendelserResponse.getForsendelser().size());
			BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = bulkOppdaterDistribusjonsinfoMapper.map(hentEkspederteForsendelserResponse);

			nPartitionJournalpost(bulkOppdaterDistribusjonsinfoRequest).forEach(journalpostWithDistribusjonsinfos -> {

				BulkOppdaterDistribusjonsinfoResponse bulkOppdaterDistribusjonsinfoResponse = bulkOppdaterDistribusjonsinfoRequest == null ? null :
						oppdaterJournalpostDistInfoConsumer.bulkOppdaterJournalpostDistribusjonsInfo(bulkOppdaterDistribusjonsinfoRequest);
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

	private Collection<List<JournalpostWithDistribusjonsinfo>> nPartitionJournalpost(BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest) {
		AtomicInteger counter = new AtomicInteger();
		return bulkOppdaterDistribusjonsinfoRequest == null ? null :
				bulkOppdaterDistribusjonsinfoRequest.getJournalposter().stream()
						.collect(Collectors.groupingBy(i -> counter.getAndIncrement() / MAX_SIZE))
						.values();
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
		return jpResult.getFeilet() == null ? 0 : jpResult.getFeilet().size();
	}

	private int countSuccess(JournalpostResultResponse jpResult) {
		return jpResult.getOppdatert() == null ? 0 : jpResult.getFeilet().size();
	}
}
