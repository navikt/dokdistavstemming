package no.nav.dokdistavstemming.consumer.journalpostapi;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JournalpostResultResponse {
	private List<JournalpostResponse> oppdatert;
	private List<JournalpostResponse> feilet;
}
