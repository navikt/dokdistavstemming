package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static no.nav.dokdistavstemming.utils.ConverterUtils.convertStringToDateTime;
import static no.nav.dokdistavstemming.utils.TestDataUtils.ARKIV_KODE;
import static no.nav.dokdistavstemming.utils.TestDataUtils.BESTILLENDE_FAGSYSTEM;
import static no.nav.dokdistavstemming.utils.TestDataUtils.BREVPRODUKSJONAPPLIKASJON;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_DATO;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_ID;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DOKUMENT_ID;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DOKUMENT_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtils.FAGOMRADE_CODE;
import static no.nav.dokdistavstemming.utils.TestDataUtils.FORSENDELSE_ID_1;
import static no.nav.dokdistavstemming.utils.TestDataUtils.FORSENDELSE_ID_2;
import static no.nav.dokdistavstemming.utils.TestDataUtils.KONVERSASJON_ID;
import static no.nav.dokdistavstemming.utils.TestDataUtils.PRODUKSJON_DATO;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokumentInfoWithForsendelseId;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createUekspedertForsendelseWithDokumenter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UekspedertForsendelseMapperTest {

	private final UekspedertForsendelseMapper mapper = new UekspedertForsendelseMapper();

	@Test
	public void shouldMapUekspedertForsendelse() {
		var forsendelse = createUekspedertForsendelseWithDokumenter(List.of(
				createDokumentInfoWithForsendelseId(FORSENDELSE_ID_1), createDokumentInfoWithForsendelseId(FORSENDELSE_ID_2))
		);
		List<UekspedertForsendelseDokument> dokumenter = mapper.mapUekspederteForsendelser(forsendelse);
		assertEquals(2, dokumenter.size());
		assertThat(dokumenter)
				.extracting(UekspedertForsendelseDokument::getForsendelseId)
				.containsExactlyInAnyOrder(FORSENDELSE_ID_1, FORSENDELSE_ID_2);

		dokumenter.forEach(this::assertDokument);
	}

	private void assertDokument(UekspedertForsendelseDokument dokument) {
		assertEquals(DISTRIBUSJON_ID, dokument.getDistribusjonId());
		assertEquals(BESTILLENDE_FAGSYSTEM, dokument.getBestillendeFagsystem());
		assertEquals(DOKUMENT_STATUS, dokument.getDokumentStatus());
		assertEquals(KONVERSASJON_ID, dokument.getKonversasjonId());
		assertEquals(ARKIV_KODE, dokument.getJournalpostId());
		assertEquals(FAGOMRADE_CODE, dokument.getFagomradeCode());
		assertEquals(DISTRIBUSJON_KANAL.name(), dokument.getDistribusjonKanal());
		assertEquals(DISTRIBUSJON_STATUS, dokument.getDistribusjonStatus());
		assertEquals(PRODUKSJON_DATO, dokument.getOpprettetDato());
		assertEquals(DISTRIBUSJON_DATO, dokument.getDistribusjonDato());
		assertEquals(DOKUMENT_ID, dokument.getDokumentId());
		assertEquals(BREVPRODUKSJONAPPLIKASJON, dokument.getBrevProduksjonApplikasjon());
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
