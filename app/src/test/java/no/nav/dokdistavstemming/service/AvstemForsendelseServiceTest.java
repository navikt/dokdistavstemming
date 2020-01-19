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
import no.nav.dokdistavstemming.service.serviceimp.CSVProdusereImpl;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_ID_3;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL_3;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS_3;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DOKUMENT_STATUS_3;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createAvstemForsendelseResponseTo;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokDistAvstemmingRequestList;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokDistAvstemmingRequestTo;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokDistAvstemmingSDPRequestTo;
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

	@Inject
	private CSVProdusere csvProdusere;

	@Mock
	private MeterRegistry meterRegistry;

	@Mock
	private JiraService jiraService;

	@Mock
	private Counter mockCounter;


	@BeforeEach
	public void setUp() {
		hentForsendelseKvitteringIkkeMottatt = mock(HentForsendelseKvitteringIkkeMottattConsumer.class);
		csvProdusere = mock(CSVProdusereImpl.class);
		argument = ArgumentCaptor.forClass(Long.class);
		avstemForsendelseService = new AvstemForsendelseService(hentForsendelseKvitteringIkkeMottatt, csvProdusere,meterRegistry,jiraService);
	}

	@Test
	void shouldCallHentForsendelseKvitteringIkkeMottattDistKanalSDP() {
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong())).thenReturn(createDokDistAvstemmingSDPRequestTo());
		Set<AvstemForsendelseRequestTo> result = avstemForsendelseService.hentForsendelserKvitteringIkkeMottattService(SDP.name());
		verify(hentForsendelseKvitteringIkkeMottatt).hentForsendelserKvitteringIkkeMottatt(anyString(), anyLong());
		assertThat(result.iterator().next().getDistribusjonKanal(),is(DISTRIBUSJON_KANAL_3.name()));
		assertThat(result.iterator().next().getDistribusjonStatus(),is(DISTRIBUSJON_STATUS_3));
		assertThat(result.iterator().next().getForsendelseId(),is(DISTRIBUSJON_ID_3));
	}

	@Test
	public void shouldFilterAndHentForsendelserDistKanalPrint(){
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong())).thenReturn(Arrays.asList(createDokDistAvstemmingRequestList().get(0)));
		when(meterRegistry.counter(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(mockCounter);

		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos =avstemForsendelseService.getMappedAvstemmForsendelseByDistKanal(PRINT.name());

		assertThat(avstemForsendelseResponseTos.get(0).getCountDokument(),is(1L));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonKanal(),is(DISTRIBUSJON_KANAL.name()));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonStatus(),is(DISTRIBUSJON_STATUS));
		verify(hentForsendelseKvitteringIkkeMottatt,times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong());

	}

	@Test
	public void shouldHentForsendelserDistKanalPrintReturnsNull(){
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong())).thenReturn(Collections.emptyList());
		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos =avstemForsendelseService.getMappedAvstemmForsendelseByDistKanal(PRINT.name());
		assertThat(avstemForsendelseResponseTos,nullValue());
		verify(hentForsendelseKvitteringIkkeMottatt,times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong());
	}


	@Test
	public void shouldHentForsendelserDistKanalUtenPrint(){
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong())).thenReturn(Arrays.asList(createDokDistAvstemmingRequestList().get(2)));
		when(meterRegistry.counter(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(mockCounter);
		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos =avstemForsendelseService.getMappedAvstemmForsendelseByDistKanal(SDP.name());

		assertThat(avstemForsendelseResponseTos.get(0).getCountDokument(),is(1L));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonKanal(),is(DISTRIBUSJON_KANAL_3.name()));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonStatus(),is(DISTRIBUSJON_STATUS_3));
		verify(hentForsendelseKvitteringIkkeMottatt,times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong());

	}

	@Test
	public void returnsNullWhenHentForsendelserKvitteringIkkeMottattGetNullorEmptyList(){

		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyLong())).thenReturn(Collections.emptyList());
		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos =avstemForsendelseService.getMappedAvstemmForsendelseByDistKanal(SDP.name());

		assertThat(avstemForsendelseResponseTos,nullValue());
	}


}
