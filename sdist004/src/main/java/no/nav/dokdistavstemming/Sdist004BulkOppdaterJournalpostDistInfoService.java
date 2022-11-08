package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.Rdist001administrerforsendelse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterJournalpostDistInfoConsumer;
import no.nav.dokdistavstemming.domain.AvstemEkspederteForsendelserRequest;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Sdist004BulkOppdaterJournalpostDistInfoService {

	private final BulkOppdaterJournalpostDistInfoConsumer oppdaterJournalpostDistInfoConsumer;
	private final Rdist001administrerforsendelse rdist001administrerforsendelse;
	private final BulkOppdaterDistribusjonsinfoMapper bulkOppdaterDistribusjonsinfoMapper;
	private final AvstemEkspederteForsendelserMapper avstemEkspederteForsendelserMapper;

	public Sdist004BulkOppdaterJournalpostDistInfoService(BulkOppdaterJournalpostDistInfoConsumer oppdaterJournalpostDistInfoConsumer,
														  Rdist001administrerforsendelse rdist001administrerforsendelse) {
		this.oppdaterJournalpostDistInfoConsumer = oppdaterJournalpostDistInfoConsumer;
		this.rdist001administrerforsendelse = rdist001administrerforsendelse;
		this.bulkOppdaterDistribusjonsinfoMapper = new BulkOppdaterDistribusjonsinfoMapper();
		this.avstemEkspederteForsendelserMapper = new AvstemEkspederteForsendelserMapper();
	}

	public void oppdaterAvstemOgJournalpostDistInfo() {

		HentEkspederteForsendelserResponse hentEkspederteForsendelserResponse = rdist001administrerforsendelse.hentEkspederteforsendelser(null);

		if (hentEkspederteForsendelserResponse != null || !hentEkspederteForsendelserResponse.getForsendelser().isEmpty()) {
			log.info("Hentet i total {} ekspederteforsendelse fra dokdist-rdist001 tjeneste.", hentEkspederteForsendelserResponse.getForsendelser().size());
			BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = bulkOppdaterDistribusjonsinfoMapper.map(hentEkspederteForsendelserResponse);

			BulkOppdaterDistribusjonsinfoResponse bulkOppdaterDistribusjonsinfoResponse = oppdaterJournalpostDistInfoConsumer.bulkOppdaterJournalpostDistribusjonsInfo(bulkOppdaterDistribusjonsinfoRequest);

			AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest = avstemEkspederteForsendelserMapper.mapAvstemEkspederteForsendelser(hentEkspederteForsendelserResponse, bulkOppdaterDistribusjonsinfoResponse.getJournalposter());
			log.info("Sdist004 oppdatert i total {} journalpost distribusjon informasjon i dokarkiv", avstemEkspederteForsendelserRequest.getForsendelser().size());
			rdist001administrerforsendelse.oppdaterAvstemEkspederteForsendelser(avstemEkspederteForsendelserRequest);
		}
	}
}
