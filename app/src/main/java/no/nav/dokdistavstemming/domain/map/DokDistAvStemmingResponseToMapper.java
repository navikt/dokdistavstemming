package no.nav.dokdistavstemming.domain.map;


import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;
import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;
import no.nav.dokdistavstemming.utils.ConverterUtils;

import static no.nav.dokdistavstemming.utils.ConverterUtils.*;
import static no.nav.dokdistavstemming.utils.ConverterUtils.convertStringToLocalDateTime;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */
public class DokDistAvStemmingResponseToMapper {


	public DokDistAvStemmingResponseTo map(HentUekspederForsendelseResponseTo uekspederResponseTo) {

		if (uekspederResponseTo.equals(null) && uekspederResponseTo.getDokumenter().equals(null)) {
			throw new DokDistAvstemmingFunctionalException("mangler dokumentinfo ");
		}

		HentUekspederForsendelseResponseTo.DokumentInfoTo dokumentInfoTo = uekspederResponseTo.getDokumenter().get(0);

		return DokDistAvStemmingResponseTo.builder()
				.forsendelseId(uekspederResponseTo.getForsendelseId())
				.konversasjonId(dokumentInfoTo.getKonversasjonId())
				.arkivKode(dokumentInfoTo.getArkivKode())
				.distribusjonDato(convertStringToLocalDateTime(uekspederResponseTo.getDistribusjonDato()))
				.produksjonDato(convertStringToLocalDateTime(uekspederResponseTo.getProduksjonDato()))
				.distribusjonKanal(stringToEnum(uekspederResponseTo.getDistribusjonKanal(), DistribusjonKanalCode.class))
				.distribusjonStatus(uekspederResponseTo.getDistribusjonStatus())
				.dokumentStatus(dokumentInfoTo.getDokumentStatus())
				.bestillendeFagsystem(dokumentInfoTo.getBestillendeFagsystem())
				.fagomradeCode(dokumentInfoTo.getFagomradeCode())
				.digitalDistributorId(dokumentInfoTo.getDigitalDistributorId())
				.mottakkerId(dokumentInfoTo.getMottakkerId())
				.countDokument(uekspederResponseTo.getCountDokument())
				.build();
	}


}
