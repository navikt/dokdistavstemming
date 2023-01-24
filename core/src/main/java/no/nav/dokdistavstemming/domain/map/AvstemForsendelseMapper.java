package no.nav.dokdistavstemming.domain.map;


import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.domain.HentUekspederteForsendelserResponse.UekspedertForsendelse;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AvstemForsendelseMapper {

	private static final String DOKUMENTINFO_FEIL ="Det Mangler dokumentInfo og avstemforsendelse kan ikke mapp dokumentInfo ";

	public List<AvstemForsendelseResponseTo> mapAvstemteForsendelser(AvstemForsendelseRequestTo forsendelseRequestTo) {

		if (forsendelseRequestTo == null) {
			throw new AvstemForsendelseFunctionalException("Fant ikke distribusjonInfo.");
		}

		List<AvstemForsendelseRequestTo.DokumentInfoTo> dokumentInfoTo = forsendelseRequestTo.getDokumenter();

		if (dokumentInfoTo == null) {
			throw new AvstemForsendelseFunctionalException(DOKUMENTINFO_FEIL);
		}

		return dokumentInfoTo.stream().filter(Objects::nonNull)
				.map(dokumentInfo -> AvstemForsendelseResponseTo.builder()
						.forsendelseId(dokumentInfo.getForsendelseId())
						.distribusjonId(forsendelseRequestTo.getDistribusjonId())
						.dokumentId(dokumentInfo.getDokumentId())
						.konversasjonId(dokumentInfo.getKonversasjonId())
						.journalpostId(dokumentInfo.getJournalpostId() == null ? null : dokumentInfo.getJournalpostId())
						.distribusjonDato(forsendelseRequestTo.getDistribusjonDato())
						.opprettetDato(forsendelseRequestTo.getOpprettetDato())
						.distribusjonKanal(forsendelseRequestTo.getDistribusjonKanal())
						.distribusjonStatus(forsendelseRequestTo.getDistribusjonStatus())
						.dokumentStatus(dokumentInfo.getDokumentStatus())
						.bestillendeFagsystem(dokumentInfo.getBestillendeFagsystem())
						.fagomradeCode(dokumentInfo.getFagomradeCode())
						.brevProduksjonApplikasjon(dokumentInfo.getBrevProduksjonApplikasjon())
						.avstemtDato(dokumentInfo.getAvstemtDato())
						.avstemtReferanse(dokumentInfo.getAvstemtReferanse())
						.build()).collect(Collectors.toList());
	}

	public static AvstemForsendelseRequestTo fromHentUekspederteForsendelserResponse(UekspedertForsendelse uekspedertForsendelse) {

		return AvstemForsendelseRequestTo.builder()
				.distribusjonId(uekspedertForsendelse.getDistribusjonId())
				.distribusjonKanal(uekspedertForsendelse.getDistribusjonKanal())
				.distribusjonStatus(uekspedertForsendelse.getDistribusjonStatus())
				.opprettetDato(uekspedertForsendelse.getOpprettetDato())
				.distribusjonDato(uekspedertForsendelse.getDistribusjonDato())
				.dokumenter(uekspedertForsendelse.getDokumenter().stream()
						.map(AvstemForsendelseMapper::fromDokumentInfoTo)
						.toList())
				.build();
	}

	private static AvstemForsendelseRequestTo.DokumentInfoTo fromDokumentInfoTo(HentUekspederteForsendelserResponse.DokumentInfoTo dokumentInfoTo) {
		return AvstemForsendelseRequestTo.DokumentInfoTo.builder()
				.forsendelseId(dokumentInfoTo.getForsendelseId())
				.dokumentId(dokumentInfoTo.getDokumentId())
				.dokumentStatus(dokumentInfoTo.getDokumentStatus())
				.konversasjonId(dokumentInfoTo.getKonversasjonId())
				.bestillendeFagsystem(dokumentInfoTo.getBestillendeFagsystem())
				.fagomradeCode(dokumentInfoTo.getFagomradeCode())
				.journalpostId(dokumentInfoTo.getJournalpostId())
				.brevProduksjonApplikasjon(dokumentInfoTo.getBrevProduksjonApplikasjon())
				.avstemtReferanse(dokumentInfoTo.getAvstemtReferanse())
				.avstemtDato(dokumentInfoTo.getAvstemtDato())
				.build();
	}

}
