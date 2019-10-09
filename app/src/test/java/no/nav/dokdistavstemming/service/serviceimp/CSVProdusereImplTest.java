package no.nav.dokdistavstemming.service.serviceimp;

import no.nav.dokdistavstemming.config.DokDistAvstemmingConfig;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingResponseTo;
import no.nav.dokdistavstemming.domain.map.DokDistAvstemmingMapper;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.utils.TestDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DokDistAvstemmingConfig.class})
class CSVProdusereImplTest {

	@Inject
	private CSVProdusere csvProdusere;


	@Test
	public void shouldProdusereCSVFil()  throws Exception{
		DokDistAvstemmingMapper mapper = new DokDistAvstemmingMapper();
		List<DokDistAvstemmingResponseTo> dokDistAvStemmingResponseTo = TestDataUtils.createDokDistAvstemmingRequestList().stream()
				.map(hentUekspederForsendelse -> mapper.mapDokDistUtenPrint(hentUekspederForsendelse))
				.collect(Collectors.toList());

		File fil = csvProdusere.oppretteCsvFil(dokDistAvStemmingResponseTo);
		long filSize = fil.length();


		assertTrue(fil.exists());
		assertTrue(fil.length()>0);

	}


}