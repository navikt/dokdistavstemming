package no.nav.dokdistavstemming.service.serviceimp;

import no.nav.dokdistavstemming.config.AvstemForsendelseConfig;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.service.CSVProdusere;
import no.nav.dokdistavstemming.utils.TestDataUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AvstemForsendelseConfig.class})
class CSVProdusereImplTest {

	@Inject
	private CSVProdusere csvProdusere;


	@Test
	public void shouldProdusereCSVFil()  throws Exception{
		AvstemForsendelseMapper mapper = new AvstemForsendelseMapper();
		List<AvstemForsendelseResponseTo> dokDistAvStemmingResponseTo = TestDataUtils.createDokDistAvstemmingRequestList().stream()
				.map(hentUekspederForsendelse -> mapper.mapDokDistUtenPrint(hentUekspederForsendelse))
				.collect(Collectors.toList());

		File fil = csvProdusere.oppretteCsvFil(dokDistAvStemmingResponseTo);
		long filSize = fil.length();


		assertTrue(fil.exists());
		assertTrue(fil.length()>0);

	}


}