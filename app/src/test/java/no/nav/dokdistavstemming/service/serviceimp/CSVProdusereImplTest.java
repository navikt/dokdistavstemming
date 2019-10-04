package no.nav.dokdistavstemming.service.serviceimp;

import no.nav.dokdistavstemming.domain.DokDistAvstemmingResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvstemmingMapper;
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
		DokDistAvstemmingMapper mapper = new DokDistAvstemmingMapper();
		List<DokDistAvstemmingResponseTo> dokDistAvStemmingResponseTo = TestDataUtils.createDokDistAvstemmingRequestList().stream()
				.map(hentUekspederForsendelse -> mapper.mapDokDistUtenPrint(hentUekspederForsendelse))
				.collect(Collectors.toList());


	}


}