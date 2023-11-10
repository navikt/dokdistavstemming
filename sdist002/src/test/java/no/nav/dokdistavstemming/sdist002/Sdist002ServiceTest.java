package no.nav.dokdistavstemming.sdist002;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.consumer.dokdistadmin.Rdist001administrerforsendelse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.Rdist001administrerforsendelseConsumer;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse.UekspedertForsendelse;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.SDP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Sdist002ServiceTest {

	@InjectMocks
	private Sdist002Service sdist002Service;

	private Rdist001administrerforsendelse hentForsendelseKvitteringIkkeMottatt;

	private CSVProdusere csvProdusere;

	@Mock
	private MeterRegistry meterRegistry;

	@Mock
	private JiraService jiraService;

	@Mock
	private Counter mockCounter;


	@BeforeEach
	public void setUp() {
		hentForsendelseKvitteringIkkeMottatt = mock(Rdist001administrerforsendelseConsumer.class);
		csvProdusere = mock(CSVProdusereImpl.class);
		DokdistavstemmingProperties dokdistavstemmingProp = new DokdistavstemmingProperties();

		sdist002Service = new Sdist002Service(hentForsendelseKvitteringIkkeMottatt, csvProdusere, meterRegistry, jiraService, dokdistavstemmingProp);
	}

	@Test
	void shouldCallHentForsendelseKvitteringIkkeMottattDistKanalSDP() {
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt())).thenReturn(TestDataUtils.createHentUekspederteForsendelserResponseSDP());

		HentUekspederteForsendelserResponse result = sdist002Service.hentForsendelserKvitteringIkkeMottattService(SDP);
		UekspedertForsendelse uekspedertForsendelse = result.getUekspederteForsendelser().get(0);

		verify(hentForsendelseKvitteringIkkeMottatt).hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt());
		MatcherAssert.assertThat(uekspedertForsendelse.getDistribusjonKanal(), CoreMatchers.is(TestDataUtils.DISTRIBUSJON_KANAL_3.name()));
		assertThat(uekspedertForsendelse.getDistribusjonStatus(), CoreMatchers.is(TestDataUtils.DISTRIBUSJON_STATUS_3));
		assertThat(uekspedertForsendelse.getDistribusjonId(), CoreMatchers.is(TestDataUtils.DISTRIBUSJON_ID_3));
	}

	@Test
	public void shouldFilterAndHentForsendelserDistKanalPrint() {
		var dokument = singletonList(TestDataUtils.createDokumentInfoWithForsendelseId(TestDataUtils.FORSENDELSE_ID_1));
		var uekspedertForsendelse = singletonList(TestDataUtils.createUekspedertForsendelseWithDokumenter(dokument));
		var response = HentUekspederteForsendelserResponse.builder().uekspederteForsendelser(uekspedertForsendelse).build();
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt())).thenReturn(response);
		when(meterRegistry.counter(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockCounter);

		List<UekspedertForsendelseDokument> uekspedertForsendelseDokumenter = sdist002Service.getForsendelserByDistribusjonKanal(PRINT);

		MatcherAssert.assertThat(uekspedertForsendelseDokumenter.get(0).getDistribusjonKanal(), CoreMatchers.is(TestDataUtils.DISTRIBUSJON_KANAL.name()));
		assertThat(uekspedertForsendelseDokumenter.get(0).getDistribusjonStatus(), CoreMatchers.is(TestDataUtils.DISTRIBUSJON_STATUS));
		verify(hentForsendelseKvitteringIkkeMottatt, times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt());
	}

	@Test
	public void shouldHentForsendelserDistKanalPrintReturnsNull() {
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt()))
				.thenReturn(HentUekspederteForsendelserResponse.builder().uekspederteForsendelser(emptyList()).build());

		List<UekspedertForsendelseDokument> uekspedertForsendelseDokumenter = sdist002Service.getForsendelserByDistribusjonKanal(PRINT);

		assertTrue(uekspedertForsendelseDokumenter.isEmpty());
		verify(hentForsendelseKvitteringIkkeMottatt, times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt());
	}


	@Test
	public void shouldHentForsendelserDistKanalUtenPrint() {
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt()))
				.thenReturn(TestDataUtils.createHentUekspederteForsendelserResponseSDP());
		when(meterRegistry.counter(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockCounter);

		List<UekspedertForsendelseDokument> uekspedertForsendelseDokumenter = sdist002Service.getForsendelserByDistribusjonKanal(SDP);

		MatcherAssert.assertThat(uekspedertForsendelseDokumenter.get(0).getDistribusjonKanal(), CoreMatchers.is(TestDataUtils.DISTRIBUSJON_KANAL_3.name()));
		assertThat(uekspedertForsendelseDokumenter.get(0).getDistribusjonStatus(), CoreMatchers.is(TestDataUtils.DISTRIBUSJON_STATUS_3));
		verify(hentForsendelseKvitteringIkkeMottatt, times(1)).hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt());
	}

	@Test
	public void shouldReturnEmptyListWhenHentForsendelserKvitteringIkkeMottattIsEmpty() {
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt()))
				.thenReturn(HentUekspederteForsendelserResponse.builder().uekspederteForsendelser(emptyList()).build());
		List<UekspedertForsendelseDokument> uekspedertForsendelseDokumenter = sdist002Service.getForsendelserByDistribusjonKanal(SDP);

		assertTrue(uekspedertForsendelseDokumenter.isEmpty());
	}

}