package no.nav.dokdistavstemming.domain.map;

import no.nav.dokdistavstemming.domain.HentUekspederteForsendelserResponse.DokumentInfo;
import no.nav.dokdistavstemming.domain.HentUekspederteForsendelserResponse.UekspedertForsendelse;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;

import java.util.List;
import java.util.Objects;

public class UekspedertForsendelseMapper {

	public List<UekspedertForsendelseDokument> mapUekspederteForsendelser(UekspedertForsendelse uekspedertForsendelse) {

		// TODO: Skift namn på AvstemForsendelseFunctionalException
		if (uekspedertForsendelse == null) {
			throw new AvstemForsendelseFunctionalException("Den uekspederte forsendelsen er null");
		}

		List<DokumentInfo> dokumentInfo = uekspedertForsendelse.getDokumenter();

		if (dokumentInfo == null) {
			throw new AvstemForsendelseFunctionalException("Den uekspederte forsendelsen mangler dokumentinfo");
		}

		return dokumentInfo.stream()
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
