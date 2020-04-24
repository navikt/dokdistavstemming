package no.nav.dokdistavstemming.domain.map;


import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */
public class AvstemForsendelseMapper {

	private static final String DOKUMENTINFO_FEIL ="Det Mangler dokumentInfo og avstemforsendelse kan ikke mapp dokumentInfo ";

	public List<AvstemForsendelseResponseTo> mapAvstemmForsendelser(AvstemForsendelseRequestTo forsendelseRequestTo) {

		if (forsendelseRequestTo == null) {
			throw new AvstemForsendelseFunctionalException("Fant ikke distribusjonInfo.");
		}

		List<AvstemForsendelseRequestTo.DokumentInfoTo> dokumentInfoTo = forsendelseRequestTo.getDokumenter();

		if (dokumentInfoTo == null) {
			throw new AvstemForsendelseFunctionalException(DOKUMENTINFO_FEIL);
		}

		return dokumentInfoTo.stream().filter(dokumentInfo-> dokumentInfo!=null)
				.map(dokumentInfo -> {
					return AvstemForsendelseResponseTo.builder()
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
							.build();
				}).collect(Collectors.toList());

	}


}
