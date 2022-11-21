package no.nav.dokdistavstemming.consumer.journalpostapi;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BulkOppdaterDistribusjonsinfoRequest {
	private List<JournalpostWithDistribusjonsinfo> journalposter;
}
