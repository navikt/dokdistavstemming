package no.nav.dokdistavstemming.sdist002.serviceimp;

import no.nav.dokdistavstemming.config.AvstemForsendelseConfig;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import no.nav.dokdistavstemming.sdist002.CSVProdusere;
import no.nav.dokdistavstemming.utils.TestDataUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = {AvstemForsendelseConfig.class})
@ExtendWith(SpringExtension.class)
class CSVProdusereImplTest {

	@Autowired
	private CSVProdusere csvProdusere;

	@Test
	public void shouldProdusereCSVFil() {
		UekspedertForsendelseMapper mapper = new UekspedertForsendelseMapper();
		List<UekspedertForsendelseDokument> dokDistAvStemmingResponseTo = TestDataUtils.createHentUekspederteForsendelserResponse().getUekspederteForsendelser().stream()
				.map(mapper::mapUekspederteForsendelser)
				.flatMap(Collection::stream)
				.toList();

		File fil = csvProdusere.oppretteCsvFil(dokDistAvStemmingResponseTo);

		assertTrue(fil.exists());
		assertTrue(fil.length() > 0);
	}
}