package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import static no.nav.dokdistavstemming.constants.DokdistavstemmingConstants.NAV_LOCAL_DATE_TIME_FORMAT;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_DATO;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.createHentUekspederteForsendelserResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class CSVProducerTest {

	private static final DateTimeFormatter NAV_LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(NAV_LOCAL_DATE_TIME_FORMAT);
	private final CSVProducer csvProdusere = new CSVProducer();

	@Test
	public void shouldProdusereCSVFil() {
		UekspedertForsendelseMapper mapper = new UekspedertForsendelseMapper();
		List<UekspedertForsendelseDokument> dokDistAvStemmingResponseTo = createHentUekspederteForsendelserResponse().getUekspederteForsendelser().stream()
				.map(mapper::mapUekspederteForsendelser)
				.flatMap(Collection::stream)
				.toList();

		byte[] csvBytes = csvProdusere.oppretteCsv(dokDistAvStemmingResponseTo, DistribusjonKanalCode.PRINT);

		assertTrue(csvBytes.length > 0);
		String csv = new String(csvBytes);
		assertThat(csv).contains(NAV_LOCAL_DATE_TIME_FORMATTER.format(DISTRIBUSJON_DATO));
		System.err.println();
	}
}