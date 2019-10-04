package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentUekspederForsendelse;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingRequestTo;
import no.nav.dokdistavstemming.metrics.MetricUtils;
import no.nav.dokdistavstemming.service.serviceimp.DokDistAvstemmingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class DokDistAvstemmingServiceTest {

	private ArgumentCaptor<Long> argument;


	private DokDistAvstemmingService dokDistAvstemmingService;

	private HentUekspederForsendelse hentUekspederForsendelse;
	private CSVProdusere csvProdusere;

	private MetricUtils metricUtils;


	@BeforeEach
	public void setUp() {
		hentUekspederForsendelse = mock(HentUekspederForsendelse.class);
		csvProdusere = mock(CSVProdusere.class);
		metricUtils=mock(MetricUtils.class);

		argument = ArgumentCaptor.forClass(Long.class);
		dokDistAvstemmingService = new DokDistAvstemmingService(hentUekspederForsendelse, csvProdusere,metricUtils);
	}

	@Test
	void shouldCallHentUekspederForsendelse() {
		List<DokDistAvstemmingRequestTo> result = dokDistAvstemmingService.hentUekspederForsendelserService(DistribusjonKanalCode.SDP.name());
		verify(hentUekspederForsendelse).hentUekspederForsendelse(DistribusjonKanalCode.SDP.name(), 6L);
		verify(hentUekspederForsendelse).hentUekspederForsendelse(anyString(), argument.capture());
		assertThat(argument.getValue().longValue(), is(6L));
	}


}
