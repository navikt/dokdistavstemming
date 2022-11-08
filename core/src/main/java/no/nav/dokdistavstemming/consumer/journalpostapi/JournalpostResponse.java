package no.nav.dokdistavstemming.consumer.journalpostapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class JournalpostResponse {
	private Long journalpostId;
	private String errormessage;

	public static JournalpostResponse ok(long journalpostId) {
		return new JournalpostResponse(journalpostId, null);
	}

	public static JournalpostResponse error(Long journalpostId, String errormessage) {
		return new JournalpostResponse(journalpostId, errormessage);
	}
}
