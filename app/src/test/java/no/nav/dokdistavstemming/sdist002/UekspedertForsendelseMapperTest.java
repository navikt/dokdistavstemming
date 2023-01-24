package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static no.nav.dokdistavstemming.utils.ConverterUtils.convertStringToDateTime;
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
import static no.nav.dokdistavstemming.utils.TestDataUtils.createUekspedertForsendelse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UekspedertForsendelseMapperTest {

	private final UekspedertForsendelseMapper mapper = new UekspedertForsendelseMapper();

	@Test
	public void shouldMapUekspedertForsendelse() {
		UekspedertForsendelseDokument dokument = mapper.mapUekspederteForsendelser(createUekspedertForsendelse()).get(0);

		assertThat(dokument.getDistribusjonId(), is(DISTRIBUSJON_ID));
		assertThat(dokument.getBestillendeFagsystem(), is(BESTILLENDE_FAGSYSTEM));
		assertThat(dokument.getDokumentStatus(), is(DOKUMENT_STATUS));
		assertThat(dokument.getKonversasjonId(), is(KONVERSASJON_ID));
		assertThat(dokument.getJournalpostId(), is(ARKIV_KODE));
		assertThat(dokument.getFagomradeCode(), is(FAGOMRADE_CODE));

		assertThat(dokument.getDistribusjonKanal(), is(DISTRIBUSJON_KANAL.name()));
		assertThat(dokument.getDistribusjonStatus(), is(DISTRIBUSJON_STATUS));
		assertThat(dokument.getOpprettetDato(), is(PRODUKSJON_DATO));
		assertThat(dokument.getDistribusjonDato(), is(DISTRIBUSJON_DATO));
	}

	@Test
	public void shouldConvertStringToDateTime(){
		String time = "2023-01-16T15:20:13.000";
		OffsetDateTime distribusjonsdato = convertStringToDateTime(time);

		assertEquals(2023, distribusjonsdato.getYear());
		assertEquals(1, distribusjonsdato.getMonthValue());
		assertEquals(16, distribusjonsdato.getDayOfMonth());
		assertEquals(15, distribusjonsdato.getHour());
		assertEquals(20, distribusjonsdato.getMinute());
		assertEquals(13, distribusjonsdato.getSecond());
	}
}
