package no.nav.dokdistavstemming.domain.map;


import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.exceptions.AvstemForsendelseFunctionalException;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */
public class AvstemForsendelseMapper {

	public AvstemForsendelseResponseTo mapDokDistPrint(AvstemForsendelseRequestTo forsendelseResponse) {

		if (forsendelseResponse == null) {
			throw new AvstemForsendelseFunctionalException("Fant ikke dokumentinfo og kan ikke mappe  ");
		}

		AvstemForsendelseRequestTo.DokumentInfoTo dokumentInfoTo = forsendelseResponse.getDokumenter().get(0);

		if (dokumentInfoTo == null) {
			throw new AvstemForsendelseFunctionalException("mangler dokumentinfo ");
		}

		return AvstemForsendelseResponseTo.builder()
				.forsendelseId(forsendelseResponse.getDistribusjonId())
				.distribusjonDato(forsendelseResponse.getDistribusjonDato())
				.produksjonDato(forsendelseResponse.getProduksjonDato() == null ? null : forsendelseResponse.getProduksjonDato())
				.distribusjonKanal(forsendelseResponse.getDistribusjonKanal())
				.distribusjonStatus(forsendelseResponse.getDistribusjonStatus())
				.dokumentStatus(forsendelseResponse.getDokumenter().get(0).getDokumentStatus() == null ? null : forsendelseResponse.getDokumenter().get(0).getDokumentStatus())
				.countDokument(forsendelseResponse.getCountDokument())
				.build();
	}

	public AvstemForsendelseResponseTo mapDokDistUtenPrint(AvstemForsendelseRequestTo forsendelseResponse) {

		if (forsendelseResponse == null) {
			throw new AvstemForsendelseFunctionalException("mangler dokumentinfo ");
		}

		AvstemForsendelseRequestTo.DokumentInfoTo dokumentInfoTo = forsendelseResponse.getDokumenter().get(0);

		if (dokumentInfoTo == null) {
			throw new AvstemForsendelseFunctionalException("mangler dokumentinfo ");
		}

		return AvstemForsendelseResponseTo.builder()
				.forsendelseId(forsendelseResponse.getDistribusjonId())
				.konversasjonId(dokumentInfoTo.getKonversasjonId())
				.journalpostId(dokumentInfoTo.getArkivKode() == null ? null : dokumentInfoTo.getArkivKode())
				.distribusjonDato(forsendelseResponse.getDistribusjonDato())
				.produksjonDato(forsendelseResponse.getProduksjonDato())
				.distribusjonKanal(forsendelseResponse.getDistribusjonKanal())
				.distribusjonStatus(forsendelseResponse.getDistribusjonStatus())
				.dokumentStatus(dokumentInfoTo.getDokumentStatus())
				.bestillendeFagsystem(dokumentInfoTo.getBestillendeFagsystem())
				.fagomradeCode(dokumentInfoTo.getFagomradeCode())
				.digitalDistributorId(dokumentInfoTo.getDigitalDistributorId())
				.mottakkerId(dokumentInfoTo.getMottakkerId())
				.countDokument(forsendelseResponse.getCountDokument())
				.build();

	}


}
