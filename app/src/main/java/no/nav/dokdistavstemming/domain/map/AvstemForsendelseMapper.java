package no.nav.dokdistavstemming.domain.map;


import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */
public class AvstemForsendelseMapper {

	private static final String DOKUMENTINFO_FEIL ="Det Mangler dokumentInfo og avstemforsendelse kan ikke mapp dokumentInfo ";
	public AvstemForsendelseResponseTo mapDokDistPrint(AvstemForsendelseRequestTo forsendelseResponse) {

		if (forsendelseResponse == null) {
			throw new AvstemForsendelseFunctionalException("Fant ikke distribusjonInfo");
		}

		AvstemForsendelseRequestTo.DokumentInfoTo dokumentInfoTo = forsendelseResponse.getDokumenter().get(0);

		if (dokumentInfoTo == null) {
			throw new AvstemForsendelseFunctionalException(DOKUMENTINFO_FEIL);
		}

		return AvstemForsendelseResponseTo.builder()
				.forsendelseId(forsendelseResponse.getForsendelseId())
				.distribusjonDato(forsendelseResponse.getDistribusjonDato())
				.opprettetDato(forsendelseResponse.getOpprettetDato() == null ? null : forsendelseResponse.getOpprettetDato())
				.distribusjonKanal(forsendelseResponse.getDistribusjonKanal())
				.distribusjonStatus(forsendelseResponse.getDistribusjonStatus())
				.dokumentStatus(forsendelseResponse.getDokumenter().get(0).getDokumentStatus() == null ? null : forsendelseResponse.getDokumenter().get(0).getDokumentStatus())
				.countDokument(forsendelseResponse.getCountDokument())
				.build();
	}

	public AvstemForsendelseResponseTo mapDokDistUtenPrint(AvstemForsendelseRequestTo forsendelseResponse) {

		if (forsendelseResponse == null) {
			throw new AvstemForsendelseFunctionalException("Fant ikke distribusjonInfo.");
		}

		AvstemForsendelseRequestTo.DokumentInfoTo dokumentInfoTo = forsendelseResponse.getDokumenter().get(0);

		if (dokumentInfoTo == null) {
			throw new AvstemForsendelseFunctionalException(DOKUMENTINFO_FEIL);
		}

		return AvstemForsendelseResponseTo.builder()
				.forsendelseId(forsendelseResponse.getForsendelseId())
				.konversasjonId(dokumentInfoTo.getKonversasjonId())
				.journalpostId(dokumentInfoTo.getJournalpostId() == null ? null : dokumentInfoTo.getJournalpostId())
				.distribusjonDato(forsendelseResponse.getDistribusjonDato())
				.opprettetDato(forsendelseResponse.getOpprettetDato())
				.distribusjonKanal(forsendelseResponse.getDistribusjonKanal())
				.distribusjonStatus(forsendelseResponse.getDistribusjonStatus())
				.dokumentStatus(dokumentInfoTo.getDokumentStatus())
				.bestillendeFagsystem(dokumentInfoTo.getBestillendeFagsystem())
				.fagomradeCode(dokumentInfoTo.getFagomradeCode())
				.digitalDistributorId(dokumentInfoTo.getDigitalDistributorId())
				.brevProduksjonApplikasjon(dokumentInfoTo.getBrevProduksjonApplikasjon())
				.countDokument(forsendelseResponse.getCountDokument())
				.build();

	}


}
