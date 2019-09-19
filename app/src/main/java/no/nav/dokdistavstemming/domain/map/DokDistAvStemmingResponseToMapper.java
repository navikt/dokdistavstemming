package no.nav.dokdistavstemming.domain.map;


import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;
import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */
public class DokDistAvStemmingResponseToMapper {


	public DokDistAvStemmingResponseTo map(HentUekspederForsendelseResponseTo uekspederForsendelseResponseTo) {

		HentUekspederForsendelseResponseTo.DokumentInfoTo dokumentInfoTo = uekspederForsendelseResponseTo.getDokumenter().get(0);

		return DokDistAvStemmingResponseTo.builder()
				.forsendelseId(uekspederForsendelseResponseTo.getForsendelseId())
				.konversasjonId(dokumentInfoTo.getKonversasjonId())
				.arkivKode(dokumentInfoTo.getArkivKode())
				.distribusjonDato(uekspederForsendelseResponseTo.getDistribusjonDato())
				.produksjonDato(uekspederForsendelseResponseTo.getProduksjonDato())
				.distribusjonKanal(uekspederForsendelseResponseTo.getDistribusjonKanal())
				.distribusjonStatus(uekspederForsendelseResponseTo.getDistribusjonStatus())
				.dokumentStatus(dokumentInfoTo.getDokumentStatus())
				.bestillendeFagsystem(dokumentInfoTo.getBestillendeFagsystem())
				.fagomradeCode(dokumentInfoTo.getFagomradeCode())
				.digitalDistributorId(dokumentInfoTo.getDigitalDistributorId())
				.mottakkerId(dokumentInfoTo.getMottakkerId())
				.countDokument(uekspederForsendelseResponseTo.getCountDokument())
				.build();

	}


}
