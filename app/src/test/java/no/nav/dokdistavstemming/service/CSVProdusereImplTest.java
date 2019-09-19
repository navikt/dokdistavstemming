package no.nav.dokdistavstemming.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokDistAvstemmingForsendelses;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
class CSVProdusereImplTest {

	@Mock
	private CSVProdusere csvProdusere;

	@BeforeEach
	public void setUp() {
		csvProdusere = mock(CSVProdusere.class);
	}

	@Test
	@DisplayName("Teste CSV producser fra DokDistAvstemming list")
	public void shouldKonvertListToCSV() throws IOException {
		csvProdusere.oppretteCsvObject(createDokDistAvstemmingForsendelses());
	}


}