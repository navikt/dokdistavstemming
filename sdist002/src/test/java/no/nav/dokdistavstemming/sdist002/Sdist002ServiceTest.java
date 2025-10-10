package no.nav.dokdistavstemming.sdist002;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.config.DokdistavstemmingProperties;
import no.nav.dokdistavstemming.consumer.dokdistadmin.DokdistadminRdist001Api;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse.UekspedertForsendelse;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_ID_3;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_KANAL_3;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_STATUS;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.DISTRIBUSJON_STATUS_3;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.FORSENDELSE_ID_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Sdist002ServiceTest {

	// @InjectMocks
	private Sdist002Service sdist002Service;

	@Mock
	private DokdistadminRdist001Api hentForsendelseKvitteringIkkeMottatt;

	@Mock
	private CSVProducer csvProdusere;

	@Mock
	private MeterRegistry meterRegistry;

	@Mock
	private JiraOppgaveService jiraOppgaveService;

	@Mock
	private Counter mockCounter;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		// hentForsendelseKvitteringIkkeMottatt = mock(DokdistadminConsumer.class);
		// csvProdusere = mock(CSVProducer.class);
		DokdistavstemmingProperties dokdistavstemmingProp = new DokdistavstemmingProperties();

		sdist002Service = new Sdist002Service(hentForsendelseKvitteringIkkeMottatt, csvProdusere, meterRegistry, jiraOppgaveService, dokdistavstemmingProp);
	}

	@Test
	void shouldCallHentForsendelseKvitteringIkkeMottattDistKanalSDP() {
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt())).thenReturn(TestDataUtils.createHentUekspederteForsendelserResponseSDP());

		HentUekspederteForsendelserResponse result = sdist002Service.hentForsendelserKvitteringIkkeMottattService(SDP);
		UekspedertForsendelse uekspedertForsendelse = result.getUekspederteForsendelser().getFirst();

		verify(hentForsendelseKvitteringIkkeMottatt).hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt());
		assertThat(uekspedertForsendelse.getDistribusjonKanal()).isEqualTo(DISTRIBUSJON_KANAL_3.name());
		assertThat(uekspedertForsendelse.getDistribusjonStatus()).isEqualTo(DISTRIBUSJON_STATUS_3);
		assertThat(uekspedertForsendelse.getDistribusjonId()).isEqualTo(DISTRIBUSJON_ID_3);
	}

	@Test
	public void shouldFilterAndHentForsendelserDistKanalPrint() {
		var dokument = singletonList(TestDataUtils.createDokumentInfoWithForsendelseId(FORSENDELSE_ID_1));
		var uekspedertForsendelse = singletonList(TestDataUtils.createUekspedertForsendelseWithDokumenter(dokument));
		var response = HentUekspederteForsendelserResponse.builder().uekspederteForsendelser(uekspedertForsendelse).build();
		when(hentForsendelseKvitteringIkkeMottatt.hentForsendelserKvitteringIkkeMottatt(anyString(), anyInt())).thenReturn(response);
		when(meterRegistry.counter(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockCounter);

		List<UekspedertForsendelseDokument> uekspedertForsendelseDokumenter = sdist002Service.getForsendelserByDistribusjonKanal(PRINT);

		assertThat(uekspedertForsendelseDokumenter.getFirst().distribusjonKanal()).isEqualTo(TestDataUtils.DISTRIBUSJON_KANAL.name());
		assertThat(uekspedertForsendelseDokumenter.getFirst().distribusjonStatus()).isEqualTo(DISTRIBUSJON_STATUS);
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

		assertThat(uekspedertForsendelseDokumenter.getFirst().distribusjonKanal()).isEqualTo(DISTRIBUSJON_KANAL_3.name());
		assertThat(uekspedertForsendelseDokumenter.getFirst().distribusjonStatus()).isEqualTo(DISTRIBUSJON_STATUS_3);
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