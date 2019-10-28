package no.nav.dokdistavstemming.service;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingRequestTo;
import no.nav.dokdistavstemming.service.serviceimp.DokDistAvstemmingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

	private HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt;
	private CSVProdusere csvProdusere;

	private MeterRegistry meterRegistry;


	@BeforeEach
	public void setUp() {
		hentForsendelseKvitteringIkkeMottatt = mock(HentForsendelseKvitteringIkkeMottatt.class);
		csvProdusere = mock(CSVProdusere.class);
		meterRegistry=mock(MeterRegistry.class);

		argument = ArgumentCaptor.forClass(Long.class);
		dokDistAvstemmingService = new DokDistAvstemmingService(hentForsendelseKvitteringIkkeMottatt, csvProdusere,meterRegistry);
	}

	@Test
	void shouldCallHentUekspederForsendelse() {
		List<DokDistAvstemmingRequestTo> result = dokDistAvstemmingService.hentUekspederForsendelserService(DistribusjonKanalCode.SDP.name());
		verify(hentForsendelseKvitteringIkkeMottatt).hentForsendelserKvitteringIkkeMottatt(DistribusjonKanalCode.SDP.name(), 24L);
		verify(hentForsendelseKvitteringIkkeMottatt).hentForsendelserKvitteringIkkeMottatt(anyString(), argument.capture());
		assertThat(argument.getValue().longValue(), is(24L));
	}




}
