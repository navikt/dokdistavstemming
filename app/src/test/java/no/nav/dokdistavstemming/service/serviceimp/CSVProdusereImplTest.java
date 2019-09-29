package no.nav.dokdistavstemming.service.serviceimp;

import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvStemmingResponseToMapper;
import no.nav.dokdistavstemming.utils.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CSVProdusereImplTest {

	@Inject
	private CSVProdusereImpl csvProdusereImp;

	@BeforeEach
	public void setUp() {
		csvProdusereImp = new CSVProdusereImpl();
	}


	@Test
	public void shouldProdusereCSVFil() {
		DokDistAvStemmingResponseToMapper mapper = new DokDistAvStemmingResponseToMapper();
		List<DokDistAvStemmingResponseTo> dokDistAvStemmingResponseTo = TestDataUtils.createDokDistAvstemmingForsendelses().stream()
				.map(hentUekspederForsendelse -> mapper.map(hentUekspederForsendelse))
				.collect(Collectors.toList());

	}

	@Test
	public void shouldOppretteCsvObjectOpenCsv() throws Exception {
		DokDistAvStemmingResponseToMapper mapper = new DokDistAvStemmingResponseToMapper();
		List<DokDistAvStemmingResponseTo> dokDistAvStemmingResponseTo = TestDataUtils.createDokDistAvstemmingForsendelses().stream()
				.map(hentUekspederForsendelse -> mapper.map(hentUekspederForsendelse))
				.collect(Collectors.toList());
		File file = csvProdusereImp.rulesToCsv(dokDistAvStemmingResponseTo);

		assertThat(file.isFile(), is(true));

	}
}