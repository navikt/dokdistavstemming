package no.nav.dokdistavstemming.domain.map;


import no.nav.dokdistavstemming.domain.DokDistAvstemmingRequestTo;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingResponseTo;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */
public class DokDistAvstemmingMapper {

	public DokDistAvstemmingResponseTo mapDokDistPrint(DokDistAvstemmingRequestTo forsendelseResponse) {

		if (forsendelseResponse==null && forsendelseResponse.getDokumenter()==null) {
			throw new DokDistAvstemmingFunctionalException("mangler dokumentinfo ");
		}
		return DokDistAvstemmingResponseTo.builder()
				.distribusjonId(forsendelseResponse.getDistribusjonId())
				.distribusjonDato(forsendelseResponse.getDistribusjonDato())
				.produksjonDato(forsendelseResponse.getProduksjonDato() == null ? null : forsendelseResponse.getProduksjonDato())
				.distribusjonKanal(forsendelseResponse.getDistribusjonKanal())
				.distribusjonStatus(forsendelseResponse.getDistribusjonStatus())
				.dokumentStatus(forsendelseResponse.getDokumenter().get(0).getDokumentStatus()==null?null:forsendelseResponse.getDokumenter().get(0).getDokumentStatus())
				.countDokument(forsendelseResponse.getCountDokument())
				.build();
	}

	public DokDistAvstemmingResponseTo mapDokDistUtenPrint(DokDistAvstemmingRequestTo forsendelseResponse) {

		if (forsendelseResponse==null && forsendelseResponse.getDokumenter()==null) {
			throw new DokDistAvstemmingFunctionalException("mangler dokumentinfo ");
		}

		DokDistAvstemmingRequestTo.DokumentInfoTo dokumentInfoTo = forsendelseResponse.getDokumenter().get(0);

		if (dokumentInfoTo==null) {
			throw new DokDistAvstemmingFunctionalException("mangler dokumentinfo ");
		}

		return DokDistAvstemmingResponseTo.builder()
				.distribusjonId(forsendelseResponse.getDistribusjonId())
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
		return "PRINT".equalsIgnoreCase(distribusjonKanal) || "SDP_PRINT".equalsIgnoreCase(distribusjonKanal);
	}

}
