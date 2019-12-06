package no.nav.dokdistavstemming.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottattConsumer;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.service.serviceimp.AvstemForsendelseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL_3;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS_3;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createAvstemForsendelseResponseTo;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokDistAvstemmingRequestList;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokDistAvstemmingRequestTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AvstemForsendelseServiceTest {

	private ArgumentCaptor<Long> argument;

	@InjectMocks
	private AvstemForsendelseService avstemForsendelseService;

	private HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt;
	private CSVProdusere csvProdusere;

	@Mock
	private MeterRegistry meterRegistry;

	@Mock
	private Counter mockCounter;

	@Mock
	private Timer mockTimer;


	@BeforeEach
	public void setUp() {
		hentForsendelseKvitteringIkkeMottatt = mock(HentForsendelseKvitteringIkkeMottattConsumer.class);
		csvProdusere = mock(CSVProdusere.class);
		argument = ArgumentCaptor.forClass(Long.class);
		avstemForsendelseService = new AvstemForsendelseService(hentForsendelseKvitteringIkkeMottatt, csvProdusere,meterRegistry);
	}

	@Test
	void shouldCallHentUekspederForsendelse() {
		Set<AvstemForsendelseRequestTo> result = avstemForsendelseService.hentForsendelserKvitteringIkkeMottattService(DistribusjonKanalCode.SDP.name());
		verify(hentForsendelseKvitteringIkkeMottatt).hentForsendelserKvitteringIkkeMottatt(DistribusjonKanalCode.SDP.name(), 24L);
		verify(hentForsendelseKvitteringIkkeMottatt).hentForsendelserKvitteringIkkeMottatt(anyString(), argument.capture());
		assertThat(argument.getValue().longValue(), is(24L));
	}

	@Test
	public void shouldFilterAndHentForsendelserDistKanalPrint(){
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong())).thenReturn(Arrays.asList(createDokDistAvstemmingRequestTo()));
		when(meterRegistry.counter(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(mockCounter);

		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos =avstemForsendelseService.avstemmForsendelseDistKanalPrint();

		assertThat(avstemForsendelseResponseTos.get(0).getCountDokument(),is(1L));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonKanal(),is(DISTRIBUSJON_KANAL.name()));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonStatus(),is(DISTRIBUSJON_STATUS));
		verify(hentForsendelseKvitteringIkkeMottatt,times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong());

	}

	@Test
	public void shouldHentForsendelserDistKanalPrintReturnEmptyList(){
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong())).thenReturn(Collections.emptyList());
		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos =avstemForsendelseService.avstemmForsendelseDistKanalPrint();
		assertThat(avstemForsendelseResponseTos,is(Collections.EMPTY_LIST));
		verify(hentForsendelseKvitteringIkkeMottatt,times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong());
	}


	@Test
	public void shouldHentForsendelserDistKanalUtenPrint(){
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong())).thenReturn(Arrays.asList(createDokDistAvstemmingRequestList().get(2)));
		when(meterRegistry.counter(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(mockCounter);
		when(meterRegistry.timer(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(mockTimer);
		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos =avstemForsendelseService.avstemmForsendelseDistKanalUtenPrint();

		assertThat(avstemForsendelseResponseTos.get(0).getCountDokument(),is(1L));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonKanal(),is(DISTRIBUSJON_KANAL_3.name()));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonStatus(),is(DISTRIBUSJON_STATUS_3));
		verify(hentForsendelseKvitteringIkkeMottatt,times(6)).hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong());

	}


}
