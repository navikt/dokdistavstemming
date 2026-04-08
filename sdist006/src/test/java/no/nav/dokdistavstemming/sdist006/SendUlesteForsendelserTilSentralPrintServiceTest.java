package no.nav.dokdistavstemming.sdist006;

import no.nav.dokdistavstemming.consumer.dokdistadmin.DokdistadminConsumer;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.ForsendelseTo;
import no.nav.dokdistavstemming.consumer.journalpostapi.DokarkivConsumer;
import no.nav.dokdistavstemming.domain.Forsendelse;
import no.nav.doknotifikasjon.schemas.DoknotifikasjonStopp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendUlesteForsendelserTilSentralPrintServiceTest {

	@Mock
	private DokdistadminConsumer dokdistadminConsumer;
	@Mock
	private DokarkivConsumer dokarkivConsumer;
	@Mock
	private DistribuerTilSentralPrintMQService distribuerTilSentralPrintMQService;
	@Mock
	private KafkaEventProducer kafkaEventProducer;

	@InjectMocks
	private SendUlesteForsendelserTilSentralPrintService service;

	@Test
	void shouldSkipForsendelseWithMoreThan100Vedlegg() {
		when(dokarkivConsumer.finnUlesteJournalposter(any(), any(), any()))
				.thenReturn(List.of("123456789"));
		when(dokdistadminConsumer.hentForsendelser(any()))
				.thenReturn(List.of(forsendelseWithDokumenter(102)));

		service.sendUlesteForsendelserTilSentralPrint();

		verify(dokdistadminConsumer, never()).opprettForsendelse(any());
		verify(dokdistadminConsumer, never()).feilregistrerForsendelse(any());
		verify(dokdistadminConsumer, never()).oppdaterForsendelse(any());
		verify(dokarkivConsumer, never()).oppdaterDistribusjonsinfo(any(), any());
		verify(distribuerTilSentralPrintMQService, never()).sendToQdist009(anyLong());
		verify(kafkaEventProducer, never()).publish(any(DoknotifikasjonStopp.class));
	}

	@Test
	void shouldProcessForsendelseWithExactly100Vedlegg() {
		when(dokarkivConsumer.finnUlesteJournalposter(any(), any(), any()))
				.thenReturn(List.of("123456789"));
		when(dokdistadminConsumer.hentForsendelser(any()))
				.thenReturn(List.of(forsendelseWithDokumenter(101)));
		when(dokdistadminConsumer.opprettForsendelse(any()))
				.thenReturn(new Forsendelse(33333L));

		service.sendUlesteForsendelserTilSentralPrint();

		verify(dokdistadminConsumer, times(1)).opprettForsendelse(any());
		verify(dokdistadminConsumer, times(1)).feilregistrerForsendelse(any());
		verify(dokdistadminConsumer, times(1)).oppdaterForsendelse(any());
		verify(dokarkivConsumer, times(1)).oppdaterDistribusjonsinfo(any(), any());
		verify(distribuerTilSentralPrintMQService, times(1)).sendToQdist009(33333L);
		verify(kafkaEventProducer, times(1)).publish(any(DoknotifikasjonStopp.class));
	}

	@Test
	void shouldSkipOnlyForsendelseWithTooManyVedleggInBatch() {
		when(dokarkivConsumer.finnUlesteJournalposter(any(), any(), any()))
				.thenReturn(List.of("123456789", "999654321"));
		when(dokdistadminConsumer.hentForsendelser(any()))
				.thenReturn(List.of(
						forsendelseWithDokumenter(102),
						forsendelseWithDokumenter(1)
				));
		when(dokdistadminConsumer.opprettForsendelse(any()))
				.thenReturn(new Forsendelse(33333L));

		service.sendUlesteForsendelserTilSentralPrint();

		verify(dokdistadminConsumer, times(1)).opprettForsendelse(any());
		verify(dokdistadminConsumer, times(1)).feilregistrerForsendelse(any());
		verify(dokdistadminConsumer, times(1)).oppdaterForsendelse(any());
		verify(dokarkivConsumer, times(1)).oppdaterDistribusjonsinfo(any(), any());
		verify(distribuerTilSentralPrintMQService, times(1)).sendToQdist009(33333L);
		verify(kafkaEventProducer, times(1)).publish(any(DoknotifikasjonStopp.class));
	}

	private ForsendelseTo forsendelseWithDokumenter(int antallDokumenter) {
		List<ForsendelseTo.Dokument> dokumenter = IntStream.range(0, antallDokumenter)
				.mapToObj(i -> ForsendelseTo.Dokument.builder()
						.tilknyttetSom(i == 0 ? "HOVEDDOKUMENT" : "VEDLEGG")
						.dokumentObjektReferanse("testKey" + i)
						.arkivDokumentInfoId(String.valueOf(1000 + i))
						.dokumenttypeId("U000001")
						.build())
				.toList();

		return ForsendelseTo.builder()
				.forsendelseId(987654321L)
				.bestillingsId("811c0c5d-test-bestilling-" + antallDokumenter)
				.arkivInformasjon(ForsendelseTo.ArkivInformasjon.builder()
						.arkivSystem("JOARK")
						.arkivId("123456789")
						.build())
				.mottaker(ForsendelseTo.Mottaker.builder()
						.mottakerId("22222222222")
						.mottakerNavn("TEST PERSON")
						.mottakerType("PERSON")
						.build())
				.dokumenter(dokumenter)
				.build();
	}
}
