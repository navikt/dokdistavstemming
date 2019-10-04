package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.DokDistAvstemmingResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvstemmingMapper;
import no.nav.dokdistavstemming.utils.ConverterUtils;
import no.nav.dokdistavstemming.utils.TestDataUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static no.nav.dokdistavstemming.utils.TestDataUtils.ARKIV_KODE;
import static no.nav.dokdistavstemming.utils.TestDataUtils.BESTILLENDE_FAGSYSTEM;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_DATO;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_ID;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DOKUMENT_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtils.FAGOMRADE_CODE;
import static no.nav.dokdistavstemming.utils.TestDataUtils.KONVERSASJON_ID;
import static no.nav.dokdistavstemming.utils.TestDataUtils.MOTTAKER_ID;
import static no.nav.dokdistavstemming.utils.TestDataUtils.PRODUKSJON_DATO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DokDistAvstemmingMapperTest {

	private DokDistAvstemmingMapper mapper = new DokDistAvstemmingMapper();


	@Test
	public void shouldHentAvstemmingForsendelseResponse() {
		DokDistAvstemmingResponseTo hentAvstemming = mapper.mapDokDistUtenPrint(TestDataUtils.createDokDistAvstemmingRequestTo());
		assertResponse(hentAvstemming);
	}

	@Test
	public void shouldConvertStringToDateTime(){
		LocalDateTime distributsionDato = ConverterUtils.convertStringToLocalDateTime(DISTRIBUSJON_DATO);
		assertThat(distributsionDato.getYear(),is(2019));
	}

	public void assertResponse(DokDistAvstemmingResponseTo dokDistAvStemmingResponseTo) {
		assertDokDistAvStemmingResponseToMapperPrint(dokDistAvStemmingResponseTo);

	}



	public void assertDokDistAvStemmingResponseToMapperPrint(DokDistAvstemmingResponseTo dokDistAvStemmingResponseTo) {
		assertThat(dokDistAvStemmingResponseTo.getDistribusjonId(), is(DISTRIBUSJON_ID));
		assertThat(dokDistAvStemmingResponseTo.getBestillendeFagsystem(), is(BESTILLENDE_FAGSYSTEM));
		assertThat(dokDistAvStemmingResponseTo.getDokumentStatus(), is(DOKUMENT_STATUS));
		assertThat(dokDistAvStemmingResponseTo.getMottakkerId(), is(MOTTAKER_ID));
		assertThat(dokDistAvStemmingResponseTo.getKonversasjonId(), is(KONVERSASJON_ID));
		assertThat(dokDistAvStemmingResponseTo.getArkivKode(), is(ARKIV_KODE));
		assertThat(dokDistAvStemmingResponseTo.getFagomradeCode(), is(FAGOMRADE_CODE));

		assertThat(dokDistAvStemmingResponseTo.getDistribusjonKanal(), is(DISTRIBUSJON_KANAL.name()));
		assertThat(dokDistAvStemmingResponseTo.getDistribusjonStatus(), is(DISTRIBUSJON_STATUS));
		assertThat(dokDistAvStemmingResponseTo.getProduksjonDato(), is(PRODUKSJON_DATO));
		assertThat(dokDistAvStemmingResponseTo.getDistribusjonDato(), is(DISTRIBUSJON_DATO));
		assertThat(dokDistAvStemmingResponseTo.getCountDokument(), is(1L));

	}




}
