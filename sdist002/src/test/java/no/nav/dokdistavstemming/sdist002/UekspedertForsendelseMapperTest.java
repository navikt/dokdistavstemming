package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.dokdistavstemming.sdist002.TestDataUtils.ARKIV_KODE;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.BESTILLENDE_FAGSYSTEM;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.BREVPRODUKSJONAPPLIKASJON;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_DATO;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_ID;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_KANAL;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_STATUS;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DOKUMENT_ID;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DOKUMENT_STATUS;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.FAGOMRADE_CODE;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.FORSENDELSE_ID_1;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.FORSENDELSE_ID_2;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.KONVERSASJON_ID;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.PRODUKSJON_DATO;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.createDokumentInfoWithForsendelseId;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.createUekspedertForsendelseWithDokumenter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UekspedertForsendelseMapperTest {

	private final UekspedertForsendelseMapper mapper = new UekspedertForsendelseMapper();

	@Test
	public void shouldMapUekspedertForsendelse() {
		var forsendelse = createUekspedertForsendelseWithDokumenter(
				List.of(createDokumentInfoWithForsendelseId(FORSENDELSE_ID_1), createDokumentInfoWithForsendelseId(FORSENDELSE_ID_2))
		);

		List<UekspedertForsendelseDokument> dokumenter = mapper.mapUekspederteForsendelser(forsendelse);

		assertEquals(2, dokumenter.size());
		assertThat(dokumenter)
				.extracting(UekspedertForsendelseDokument::forsendelseId)
				.containsExactlyInAnyOrder(FORSENDELSE_ID_1, FORSENDELSE_ID_2);

		dokumenter.forEach(this::assertDokument);
	}

	private void assertDokument(UekspedertForsendelseDokument dokument) {
		assertEquals(DISTRIBUSJON_ID, dokument.distribusjonId());
		assertEquals(BESTILLENDE_FAGSYSTEM, dokument.bestillendeFagsystem());
		assertEquals(DOKUMENT_STATUS, dokument.dokumentStatus());
		assertEquals(KONVERSASJON_ID, dokument.konversasjonId());
		assertEquals(ARKIV_KODE, dokument.journalpostId());
		assertEquals(FAGOMRADE_CODE, dokument.fagomradeCode());
		assertEquals(DISTRIBUSJON_KANAL.name(), dokument.distribusjonKanal());
		assertEquals(DISTRIBUSJON_STATUS, dokument.distribusjonStatus());
		assertEquals(PRODUKSJON_DATO, dokument.opprettetDato());
		assertEquals(DISTRIBUSJON_DATO, dokument.distribusjonDato());
		assertEquals(DOKUMENT_ID, dokument.dokumentId());
		assertEquals(BREVPRODUKSJONAPPLIKASJON, dokument.brevProduksjonApplikasjon());
	}
}
