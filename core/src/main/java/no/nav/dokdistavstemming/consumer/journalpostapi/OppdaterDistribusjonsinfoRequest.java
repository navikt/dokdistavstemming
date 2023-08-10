package no.nav.dokdistavstemming.consumer.journalpostapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
@AllArgsConstructor
public class OppdaterDistribusjonsinfoRequest {

	private Boolean settStatusEkspedert;
	private String utsendingsKanal;
	private Boolean tilbakestillJournalpost;

}
