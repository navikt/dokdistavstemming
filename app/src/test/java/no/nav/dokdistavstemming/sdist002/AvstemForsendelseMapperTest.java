package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.utils.ConverterUtils;
import no.nav.dokdistavstemming.utils.TestDataUtils;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static no.nav.dokdistavstemming.utils.TestDataUtils.ARKIV_KODE;
import static no.nav.dokdistavstemming.utils.TestDataUtils.BESTILLENDE_FAGSYSTEM;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_DATO;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_ID;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DOKUMENT_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtils.FAGOMRADE_CODE;
import static no.nav.dokdistavstemming.utils.TestDataUtils.KONVERSASJON_ID;
import static no.nav.dokdistavstemming.utils.TestDataUtils.PRODUKSJON_DATO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AvstemForsendelseMapperTest {

	private final AvstemForsendelseMapper mapper = new AvstemForsendelseMapper();

	@Test
	public void shouldHentAvstemmingForsendelseResponse() {
		AvstemForsendelseResponseTo hentAvstemming = mapper.mapAvstemteForsendelser(TestDataUtils.createDokDistAvstemmingRequestTo()).get(0);
		assertResponse(hentAvstemming);
	}

	@Test
	public void shouldConvertStringToDateTime(){
		OffsetDateTime distribusjonsdato = ConverterUtils.convertStringToDateTime(DISTRIBUSJON_DATO);
		assertThat(distribusjonsdato.getYear(), is(2022));
	}

	public void assertResponse(AvstemForsendelseResponseTo dokDistAvStemmingResponseTo) {
		assertDokDistAvStemmingResponseToMapperPrint(dokDistAvStemmingResponseTo);

	}

	public void assertDokDistAvStemmingResponseToMapperPrint(AvstemForsendelseResponseTo dokDistAvStemmingResponseTo) {
		assertThat(dokDistAvStemmingResponseTo.getDistribusjonId(), is(DISTRIBUSJON_ID));
		assertThat(dokDistAvStemmingResponseTo.getBestillendeFagsystem(), is(BESTILLENDE_FAGSYSTEM));
		assertThat(dokDistAvStemmingResponseTo.getDokumentStatus(), is(DOKUMENT_STATUS));
		assertThat(dokDistAvStemmingResponseTo.getKonversasjonId(), is(KONVERSASJON_ID));
		assertThat(dokDistAvStemmingResponseTo.getJournalpostId(), is(ARKIV_KODE));
		assertThat(dokDistAvStemmingResponseTo.getFagomradeCode(), is(FAGOMRADE_CODE));

		assertThat(dokDistAvStemmingResponseTo.getDistribusjonKanal(), is(DISTRIBUSJON_KANAL.name()));
		assertThat(dokDistAvStemmingResponseTo.getDistribusjonStatus(), is(DISTRIBUSJON_STATUS));
		assertThat(dokDistAvStemmingResponseTo.getOpprettetDato(), is(PRODUKSJON_DATO));
		assertThat(dokDistAvStemmingResponseTo.getDistribusjonDato(), is(DISTRIBUSJON_DATO));

	}
}
