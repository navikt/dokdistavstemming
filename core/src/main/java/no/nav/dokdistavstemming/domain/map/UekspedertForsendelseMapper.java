package no.nav.dokdistavstemming.domain.map;

import no.nav.dokdistavstemming.domain.HentUekspederteForsendelserResponse.UekspedertForsendelse;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;

import java.util.List;
import java.util.Objects;

public class UekspedertForsendelseMapper {

	public List<UekspedertForsendelseDokument> mapUekspederteForsendelser(UekspedertForsendelse uekspedertForsendelse) {

		return uekspedertForsendelse.getDokumenter().stream()
				.filter(Objects::nonNull)
				.map(dok -> UekspedertForsendelseDokument.builder()
						.forsendelseId(dok.getForsendelseId())
						.distribusjonId(uekspedertForsendelse.getDistribusjonId())
						.dokumentId(dok.getDokumentId())
						.konversasjonId(dok.getKonversasjonId())
						.journalpostId(dok.getJournalpostId() == null ? null : dok.getJournalpostId())
						.distribusjonDato(uekspedertForsendelse.getDistribusjonDato())
						.opprettetDato(uekspedertForsendelse.getOpprettetDato())
						.distribusjonKanal(uekspedertForsendelse.getDistribusjonKanal())
						.distribusjonStatus(uekspedertForsendelse.getDistribusjonStatus())
						.dokumentStatus(dok.getDokumentStatus())
						.bestillendeFagsystem(dok.getBestillendeFagsystem())
						.fagomradeCode(dok.getFagomradeCode())
						.brevProduksjonApplikasjon(dok.getBrevProduksjonApplikasjon())
						.build())
				.toList();
	}
}
