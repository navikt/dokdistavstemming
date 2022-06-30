package no.nav.dokdistavstemming.service.serviceimp;

import no.nav.dokdistavstemming.config.AvstemForsendelseConfig;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.utils.TestDataUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = {AvstemForsendelseConfig.class})
@ExtendWith(SpringExtension.class)
class CSVProdusereImplTest {

	@Autowired
	private CSVProdusere csvProdusere;

	@Test
	public void shouldProdusereCSVFil() {
		AvstemForsendelseMapper mapper = new AvstemForsendelseMapper();
		List<AvstemForsendelseResponseTo> dokDistAvStemmingResponseTo = TestDataUtils.createDokDistAvstemmingRequestList().stream()
				.map(mapper::mapAvstemmedeForsendelser)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		File fil = csvProdusere.oppretteCsvFil(dokDistAvStemmingResponseTo);

		assertTrue(fil.exists());
		assertTrue(fil.length() > 0);

	}
}