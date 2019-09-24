package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvStemmingResponseToMapper;
import no.nav.dokdistavstemming.utils.TestDataUtils;
import org.junit.jupiter.api.Test;

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

public class DokDistAvStemmingResponseToMapperTest {

	private DokDistAvStemmingResponseToMapper mapper = new DokDistAvStemmingResponseToMapper();

	@Test
	public void shouldHentAvstemmingForsendelseResponse() {
		DokDistAvStemmingResponseTo hentAvstemming = mapper.map(TestDataUtils.createDokDistAvstemmingForsendels());
		assertResponse(hentAvstemming);
	}


	public void assertResponse(DokDistAvStemmingResponseTo hentUekspederKvitteringForsendelse) {
		assertDokDistAvStemmingResponseToMapperPrint(hentUekspederKvitteringForsendelse);

	}

	public void assertDokDistAvStemmingResponseToMapperPrint(DokDistAvStemmingResponseTo dokDistAvStemmingResponseTo) {
		assertThat(dokDistAvStemmingResponseTo.getForsendelseId(), is(DISTRIBUSJON_ID));
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
