package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static no.nav.dokdistavstemming.sdist002.TestDataUtils.createHentUekspederteForsendelserResponse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class CSVProdusereImplTest {

	private final CSVProdusere csvProdusere = new CSVProdusereImpl();

	@Test
	public void shouldProdusereCSVFil() {
		UekspedertForsendelseMapper mapper = new UekspedertForsendelseMapper();
		List<UekspedertForsendelseDokument> dokDistAvStemmingResponseTo = createHentUekspederteForsendelserResponse().getUekspederteForsendelser().stream()
				.map(mapper::mapUekspederteForsendelser)
				.flatMap(Collection::stream)
				.toList();

		File fil = csvProdusere.oppretteCsvFil(dokDistAvStemmingResponseTo);

		assertTrue(fil.exists());
		assertTrue(fil.length() > 0);
	}
}