package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.List;

import static no.nav.dokdistavstemming.sdist002.TestDataUtils.createHentUekspederteForsendelserResponse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class CSVProducerTest {

	private final CSVProducer csvProdusere = new CSVProducer();

	@Test
	public void shouldProdusereCSVFil() {
		UekspedertForsendelseMapper mapper = new UekspedertForsendelseMapper();
		List<UekspedertForsendelseDokument> dokDistAvStemmingResponseTo = createHentUekspederteForsendelserResponse().getUekspederteForsendelser().stream()
				.map(mapper::mapUekspederteForsendelser)
				.flatMap(Collection::stream)
				.toList();

		byte[] csv = csvProdusere.oppretteCsv(dokDistAvStemmingResponseTo, DistribusjonKanalCode.PRINT);

		assertTrue(csv.length > 0);
	}
}