package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static no.nav.dokdistavstemming.utils.ConverterUtils.convertStringToDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UekspedertForsendelseMapperTest {

	private final UekspedertForsendelseMapper mapper = new UekspedertForsendelseMapper();

	@Test
	public void shouldMapUekspedertForsendelse() {
		var forsendelse = TestDataUtils.createUekspedertForsendelseWithDokumenter(List.of(
				TestDataUtils.createDokumentInfoWithForsendelseId(TestDataUtils.FORSENDELSE_ID_1), TestDataUtils.createDokumentInfoWithForsendelseId(TestDataUtils.FORSENDELSE_ID_2))
		);
		List<UekspedertForsendelseDokument> dokumenter = mapper.mapUekspederteForsendelser(forsendelse);
		assertEquals(2, dokumenter.size());
		assertThat(dokumenter)
				.extracting(UekspedertForsendelseDokument::getForsendelseId)
				.containsExactlyInAnyOrder(TestDataUtils.FORSENDELSE_ID_1, TestDataUtils.FORSENDELSE_ID_2);

		dokumenter.forEach(this::assertDokument);
	}

	private void assertDokument(UekspedertForsendelseDokument dokument) {
		Assertions.assertEquals(TestDataUtils.DISTRIBUSJON_ID, dokument.getDistribusjonId());
		Assertions.assertEquals(TestDataUtils.BESTILLENDE_FAGSYSTEM, dokument.getBestillendeFagsystem());
		Assertions.assertEquals(TestDataUtils.DOKUMENT_STATUS, dokument.getDokumentStatus());
		Assertions.assertEquals(TestDataUtils.KONVERSASJON_ID, dokument.getKonversasjonId());
		Assertions.assertEquals(TestDataUtils.ARKIV_KODE, dokument.getJournalpostId());
		Assertions.assertEquals(TestDataUtils.FAGOMRADE_CODE, dokument.getFagomradeCode());
		Assertions.assertEquals(TestDataUtils.DISTRIBUSJON_KANAL.name(), dokument.getDistribusjonKanal());
		Assertions.assertEquals(TestDataUtils.DISTRIBUSJON_STATUS, dokument.getDistribusjonStatus());
		Assertions.assertEquals(TestDataUtils.PRODUKSJON_DATO, dokument.getOpprettetDato());
		Assertions.assertEquals(TestDataUtils.DISTRIBUSJON_DATO, dokument.getDistribusjonDato());
		Assertions.assertEquals(TestDataUtils.DOKUMENT_ID, dokument.getDokumentId());
		Assertions.assertEquals(TestDataUtils.BREVPRODUKSJONAPPLIKASJON, dokument.getBrevProduksjonApplikasjon());
	}

	@Test
	public void shouldConvertStringToDateTime() {
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
