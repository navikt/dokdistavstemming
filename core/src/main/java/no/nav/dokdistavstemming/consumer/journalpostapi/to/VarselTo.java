package no.nav.dokdistavstemming.consumer.journalpostapi.to;

import java.util.List;

public record VarselTo(List<EpostvarselTo> epostvarsel, List<SmsvarselTo> smsvarsel) {
}
