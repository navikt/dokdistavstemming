package no.nav.dokdistavstemming.itest;


import no.nav.dokdistavstemming.Sdist004BulkOppdaterJournalpostDistInfoService;
import no.nav.dokdistavstemming.config.AbstractIT;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.Rdist001administrerforsendelse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterJournalpostDistInfoConsumer;
import no.nav.dokdistavstemming.utils.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static no.nav.dokdistavstemming.utils.WireMockResponse.getEkspederteForsendelser;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterAvstemArkivFrosendelseInfo;
import static no.nav.dokdistavstemming.utils.WireMockResponse.oppdaterJournalpost;


@Disabled
public class BulkOppdaterJournalpostDistInfoServiceITest extends AbstractIT {

	@Autowired
	private Sdist004BulkOppdaterJournalpostDistInfoService sdist004BulkOppdaterService;

	@Autowired
	private Rdist001administrerforsendelse administrerforsendelse;

	@Autowired
	private BulkOppdaterJournalpostDistInfoConsumer bulkOppdaterJournalpostDistInfo;


	@BeforeEach
	public void setUp() {
		 sdist004BulkOppdaterService = new Sdist004BulkOppdaterJournalpostDistInfoService(bulkOppdaterJournalpostDistInfo, administrerforsendelse);
	}

	@Test
	public void shouldHenteBulkForsendelseOgOppdatereJouralpost() throws Exception {
		getEkspederteForsendelser();
		oppdaterAvstemArkivFrosendelseInfo();
		oppdaterJournalpost();


		sdist004BulkOppdaterService.oppdaterAvstemOgJournalpostDistInfo();
	}
}
