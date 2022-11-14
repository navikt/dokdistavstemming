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
		HentEkspederteForsendelserResponse hentEkspederteForsendelserResponse = rdist001administrerforsendelse.hentEkspederteforsendelser();

		if (hentEkspederteForsendelserResponse != null || isValidForsendelse(hentEkspederteForsendelserResponse)) {
			log.info("sdist004 hentet totalt {} ekspederteforsendelse fra dokdist-rdist001.", hentEkspederteForsendelserResponse.getForsendelser().size());
			BulkOppdaterDistribusjonsinfoRequest bulkOppdaterDistribusjonsinfoRequest = bulkOppdaterDistribusjonsinfoMapper.map(hentEkspederteForsendelserResponse);

			BulkOppdaterDistribusjonsinfoResponse bulkOppdaterDistribusjonsinfoResponse = oppdaterJournalpostDistInfoConsumer.bulkOppdaterJournalpostDistribusjonsInfo(bulkOppdaterDistribusjonsinfoRequest);

			AvstemEkspederteForsendelserRequest avstemEkspederteForsendelserRequest = avstemEkspederteForsendelserMapper.mapAvstemEkspederteForsendelser(hentEkspederteForsendelserResponse, bulkOppdaterDistribusjonsinfoResponse.getJournalposter());

			if (avstemEkspederteForsendelserRequest != null) {
				log.info("sdist004 oppdaterte totalt {} journalposter distribusjon informasjon på dokarkiv", bulkOppdaterDistribusjonsinfoResponse.getJournalposter().getOppdatert().size());
				rdist001administrerforsendelse.oppdaterAvstemEkspederteForsendelser(avstemEkspederteForsendelserRequest);
			}
		}
	}

	private boolean isValidForsendelse(HentEkspederteForsendelserResponse hentEkspederteForsendelser) {
		return hentEkspederteForsendelser.getForsendelser() != null || hentEkspederteForsendelser.getForsendelser().isEmpty();
	}
}
