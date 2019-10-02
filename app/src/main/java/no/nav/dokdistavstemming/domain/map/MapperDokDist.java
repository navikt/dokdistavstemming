package no.nav.dokdistavstemming.domain.map;

import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.to.DokDistAvstemmingUtenPrintTo;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

public class MapperDokDist {


	public DokDistAvstemmingUtenPrintTo mapDokDistPrint(HentUekspederForsendelseResponseTo forsendelseResponse) {

		if (forsendelseResponse.equals(null) && forsendelseResponse.getDokumenter().equals(null)) {
			throw new DokDistAvstemmingFunctionalException("mangler dokumentinfo ");
		}

		return DokDistAvstemmingUtenPrintTo.builder()
				.forsendelseId(forsendelseResponse.getForsendelseId())
				.distribusjonDato(forsendelseResponse.getDistribusjonDato())
				.produksjonDato(forsendelseResponse.getProduksjonDato() == null ? null : forsendelseResponse.getProduksjonDato())
				.distribusjonKanal(forsendelseResponse.getDistribusjonKanal())
				.distribusjonStatus(forsendelseResponse.getDistribusjonStatus())
				.dokumentStatus(forsendelseResponse.getDistribusjonStatus())
				.countDokument(forsendelseResponse.getCountDokument())
				.build();
	}

	public DokDistAvstemmingUtenPrintTo mapDokDistUtenPrint(HentUekspederForsendelseResponseTo forsendelseResponse) {

		if (forsendelseResponse.equals(null) && forsendelseResponse.getDokumenter().equals(null)) {
			throw new DokDistAvstemmingFunctionalException("mangler dokumentinfo ");
		}

		HentUekspederForsendelseResponseTo.DokumentInfoTo dokumentInfoTo = forsendelseResponse.getDokumenter().get(0);

		if (dokumentInfoTo.equals(null)) {
			throw new DokDistAvstemmingFunctionalException("mangler dokumentinfo ");
		}

		return DokDistAvstemmingUtenPrintTo.builder()
				.forsendelseId(forsendelseResponse.getForsendelseId())
				.konversasjonId(dokumentInfoTo.getKonversasjonId())
				.arkivKode(dokumentInfoTo.getArkivKode() == null ? null : dokumentInfoTo.getArkivKode())
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

	private boolean isDistribusjonKanalPrint(String distribusjonKanal) {
		return distribusjonKanal.equalsIgnoreCase("PRINT") || distribusjonKanal.equalsIgnoreCase("SDP_PRINT");
	}
}