package no.nav.dokdistavstemming.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.DokdistavstemmingProp;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottatt;
import no.nav.dokdistavstemming.consumer.dokumentdistribusjon.HentForsendelseKvitteringIkkeMottattConsumer;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.service.serviceimp.CSVProdusereImpl;
import no.nav.dokdistavstemming.service.serviceimp.JiraService;
import no.nav.dokdistavstemming.service.serviceimp.Sdist002Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_ID_3;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_KANAL_3;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS;
import static no.nav.dokdistavstemming.utils.TestDataUtils.DISTRIBUSJON_STATUS_3;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokDistAvstemmingRequestList;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokDistAvstemmingSDPRequestTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@ExtendWith(MockitoExtension.class)
public class Sdist002ServiceTest {

	@InjectMocks
	private Sdist002Service sdist002Service;

	private HentForsendelseKvitteringIkkeMottatt hentForsendelseKvitteringIkkeMottatt;

	@Autowired
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
		DokdistavstemmingProp dokdistavstemmingProp = new DokdistavstemmingProp();
		
		sdist002Service = new Sdist002Service(hentForsendelseKvitteringIkkeMottatt, csvProdusere,meterRegistry,jiraService, dokdistavstemmingProp);
	}

	@Test
	void shouldCallHentForsendelseKvitteringIkkeMottattDistKanalSDP() {
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyInt())).thenReturn(createDokDistAvstemmingSDPRequestTo());
		Set<AvstemForsendelseRequestTo> result = sdist002Service.hentForsendelserKvitteringIkkeMottattService(SDP.name());
		verify(hentForsendelseKvitteringIkkeMottatt).hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt());
		assertThat(result.iterator().next().getDistribusjonKanal(),is(DISTRIBUSJON_KANAL_3.name()));
		assertThat(result.iterator().next().getDistribusjonStatus(),is(DISTRIBUSJON_STATUS_3));
		assertThat(result.iterator().next().getDistribusjonId(),is(DISTRIBUSJON_ID_3));
	}

	@Test
	public void shouldFilterAndHentForsendelserDistKanalPrint(){
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyInt())).thenReturn(singletonList(createDokDistAvstemmingRequestList().get(0)));
		when(meterRegistry.counter(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(mockCounter);

		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos = sdist002Service.getForsendelserByDistribusjonKanal(PRINT.name());

		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonKanal(),is(DISTRIBUSJON_KANAL.name()));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonStatus(),is(DISTRIBUSJON_STATUS));
		verify(hentForsendelseKvitteringIkkeMottatt,times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(),anyInt());

	}

	@Test
	public void shouldHentForsendelserDistKanalPrintReturnsNull(){
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyInt())).thenReturn(Collections.emptyList());
		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos = sdist002Service.getForsendelserByDistribusjonKanal(PRINT.name());
		assertThat(avstemForsendelseResponseTos,nullValue());
		verify(hentForsendelseKvitteringIkkeMottatt,times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(),anyInt());
	}


	@Test
	public void shouldHentForsendelserDistKanalUtenPrint(){
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyInt())).thenReturn(singletonList(createDokDistAvstemmingRequestList().get(2)));
		when(meterRegistry.counter(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(mockCounter);
		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos = sdist002Service.getForsendelserByDistribusjonKanal(SDP.name());

		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonKanal(),is(DISTRIBUSJON_KANAL_3.name()));
		assertThat(avstemForsendelseResponseTos.get(0).getDistribusjonStatus(),is(DISTRIBUSJON_STATUS_3));
		verify(hentForsendelseKvitteringIkkeMottatt,times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(),anyInt());

	}

	@Test
	public void returnsNullWhenHentForsendelserKvitteringIkkeMottattGetNullorEmptyList(){

		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(),anyInt())).thenReturn(Collections.emptyList());
		List<AvstemForsendelseResponseTo> avstemForsendelseResponseTos = sdist002Service.getForsendelserByDistribusjonKanal(SDP.name());

		assertThat(avstemForsendelseResponseTos,nullValue());
	}

}
