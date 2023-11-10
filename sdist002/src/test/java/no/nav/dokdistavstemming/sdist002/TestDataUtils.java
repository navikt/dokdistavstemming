package no.nav.dokdistavstemming.sdist002;

import io.micrometer.core.instrument.util.IOUtils;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse.DokumentInfo;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentUekspederteForsendelserResponse.UekspedertForsendelse;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Collections.singletonList;

public class TestDataUtils {
	public static final String KONVERSASJON_ID = "7ef3e7c7-cd4c-40bd-a5bf-99c5dbb26131";
	public static final String DISTRIBUSJON_ID = "7882d37e-34f7-11e9-b677-d663bd953d61";
	public static final String DOKUMENT_ID = "1234d37e-34f7-11e9-b677-d663bd953d61";
	public static final String BESTILLENDE_FAGSYSTEM = "ARENA";
	public static final String FAGOMRADE_CODE = "DAG";
	public static final String ARKIV_KODE = "389426100";
	public static final String DOKUMENT_STATUS = "OPPRETTET";
	public static final String BREVPRODUKSJONAPPLIKASJON = "OEBS_HANDEL";
	public static final String DISTRIBUSJON_STATUS = "OPPRETTET";
	public static final DistribusjonKanalCode DISTRIBUSJON_KANAL = DistribusjonKanalCode.PRINT;
	public static final String KONVERSASJON_ID_2 = "7ef3e7c7-cd4c-40bd-a5bf-99c5dbb26132";
	public static final String DISTRIBUSJON_ID_2 = "7882d37e-34f7-11e9-b677-d663bd953d62";
	public static final String BESTILLENDE_FAGSYSTEM_2 = "ARENA";
	public static final String FAGOMRADE_CODE_2 = "DAG";
	public static final String ARKIV_KODE_2 = "389426102";
	public static final String DOKUMENT_STATUS_2 = "OPPRETTET";
	public static final String DISTRIBUSJON_STATUS_2 = "OPPRETTET";
	public static final DistribusjonKanalCode DISTRIBUSJON_KANAL_2 = DistribusjonKanalCode.SDP;
	public static final String KONVERSASJON_ID_3 = "7ef3e7c7-cd4c-40bd-a5bf-99c5dbb26143";
	public static final String DISTRIBUSJON_ID_3 = "7882d37e-34f7-11e9-b677-d663bd953d63";
	public static final String BESTILLENDE_FAGSYSTEM_3 = "ARENA";
	public static final String FAGOMRADE_CODE_3 = "AAP";
	public static final OffsetDateTime OPPRETTET_DATO_3 = OffsetDateTime.now().minusHours(6).minusSeconds(3);
	public static final OffsetDateTime DISTRIBUSJON_DATO_3 = OffsetDateTime.now().minusHours(6).minusMinutes(1).minusSeconds(59);
	public static final String MOTTAKER_ID_3 = "26016826023";
	public static final String ARKIV_KODE_3 = "389426113";
	public static final String DOKUMENT_STATUS_3 = "OVERSENDT";
	public static final String DISTRIBUSJON_STATUS_3 = "OVERSENDT";
	public static final DistribusjonKanalCode DISTRIBUSJON_KANAL_3 = DistribusjonKanalCode.SDP;
	public static final String FORSENDELSE_ID_1 = "1";
	public static final String FORSENDELSE_ID_2 = "2";
	public static final String FORSENDELSE_ID_3 = "3";
	private static final String DATE_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String PRODUKSJON_DATO = convertDateTimeToString(OffsetDateTime.now().now().minusDays(6).minusMinutes(23));
	public static final String DISTRIBUSJON_DATO = convertDateTimeToString(OffsetDateTime.now().minusDays(7).minusHours(23).minusMinutes(59));
	public static final String PRODUKSJON_DATO_2 = convertDateTimeToString(OffsetDateTime.now().minusDays(6).minusMinutes(23));
	public static final String DISTRIBUSJON_DATO_2 = convertDateTimeToString(OffsetDateTime.now().minusHours(6).minusSeconds(59));
	public static String DISTRIBUSJON_ID_PRINT = "c3bc9d18-c5b8-40d2-9d50-5cbfa661475c";
	public static String DISTRIBUSJON_ID_SDP = "39df91f9-173b-4bf4-89ec-2a9577f227fb";
	public static String DISTRIBUSJON_STATUS_J = "OVERSENDT";
	public static String DISRIBUSJON_DATO_J = "2020-01-14 13:00:48";
	public static DistribusjonKanalCode DISTRIBUSJON_KANAL_P_J = DistribusjonKanalCode.PRINT;
	public static String AVSTEMT_REFERANSE = "MMA-1234";

	public static HentUekspederteForsendelserResponse createHentUekspederteForsendelserResponse() {
		return HentUekspederteForsendelserResponse.builder()
				.uekspederteForsendelser(List.of(
						UekspedertForsendelse.builder()
								.distribusjonId(DISTRIBUSJON_ID)
								.distribusjonKanal(DISTRIBUSJON_KANAL.name())
								.distribusjonStatus(DISTRIBUSJON_STATUS)
								.opprettetDato(PRODUKSJON_DATO)
								.distribusjonDato(DISTRIBUSJON_DATO)
								.dokumenter(singletonList(DokumentInfo.builder()
										.forsendelseId(FORSENDELSE_ID_1)
										.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
										.dokumentStatus(DOKUMENT_STATUS)
										.konversasjonId(KONVERSASJON_ID)
										.journalpostId(ARKIV_KODE)
										.fagomradeCode(FAGOMRADE_CODE)
										.build()))
								.build(),
						UekspedertForsendelse.builder().distribusjonId(DISTRIBUSJON_ID_2)
								.distribusjonKanal(DISTRIBUSJON_KANAL_2.name())
								.distribusjonStatus(DISTRIBUSJON_STATUS_2)
								.opprettetDato(PRODUKSJON_DATO_2)
								.distribusjonDato(DISTRIBUSJON_DATO_2)
								.dokumenter(singletonList(DokumentInfo.builder()
										.forsendelseId(FORSENDELSE_ID_2)
										.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM_2)
										.dokumentStatus(DOKUMENT_STATUS_2)
										.fagomradeCode(FAGOMRADE_CODE_2)
										.konversasjonId(KONVERSASJON_ID_2)
										.journalpostId(ARKIV_KODE_2)
										.build()))
								.build(),
						UekspedertForsendelse.builder()
								.distribusjonId(DISTRIBUSJON_ID_3)
								.distribusjonKanal(DISTRIBUSJON_KANAL_3.name())
								.distribusjonStatus(DISTRIBUSJON_STATUS_3)
								.opprettetDato(convertDateTimeToString(OPPRETTET_DATO_3))
								.distribusjonDato(convertDateTimeToString(DISTRIBUSJON_DATO_3))
								.dokumenter(singletonList(DokumentInfo.builder()
										.forsendelseId(FORSENDELSE_ID_3)
										.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM_3)
										.dokumentStatus(DOKUMENT_STATUS_3)
										.fagomradeCode(FAGOMRADE_CODE_3)
										.konversasjonId(KONVERSASJON_ID_3)
										.journalpostId(ARKIV_KODE_3)
										.build()))
								.build())
				).build();
	}

	public static UekspedertForsendelse createUekspedertForsendelseWithDokumenter(List<DokumentInfo> dokumenter) {
		return UekspedertForsendelse.builder()
				.distribusjonId(DISTRIBUSJON_ID)
				.distribusjonKanal(DISTRIBUSJON_KANAL.name())
				.distribusjonStatus(DISTRIBUSJON_STATUS)
				.opprettetDato(PRODUKSJON_DATO)
				.distribusjonDato(DISTRIBUSJON_DATO)
				.dokumenter(dokumenter)
				.build();
	}

	public static DokumentInfo createDokumentInfoWithForsendelseId(String forsendelseId) {
		return DokumentInfo.builder()
				.forsendelseId(forsendelseId)
				.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
				.dokumentStatus(DOKUMENT_STATUS)
				.konversasjonId(KONVERSASJON_ID)
				.journalpostId(ARKIV_KODE)
				.fagomradeCode(FAGOMRADE_CODE)
				.dokumentId(DOKUMENT_ID)
				.brevProduksjonApplikasjon(BREVPRODUKSJONAPPLIKASJON)
				.build();
	}

	public static HentUekspederteForsendelserResponse createHentUekspederteForsendelserResponseSDP() {
		return HentUekspederteForsendelserResponse.builder()
				.uekspederteForsendelser(singletonList(
						UekspedertForsendelse.builder()
								.distribusjonId(DISTRIBUSJON_ID_3)
								.distribusjonKanal(DISTRIBUSJON_KANAL_3.name())
								.distribusjonStatus(DISTRIBUSJON_STATUS_3)
								.opprettetDato(convertDateTimeToString(OPPRETTET_DATO_3))
								.distribusjonDato(convertDateTimeToString(DISTRIBUSJON_DATO_3))
								.dokumenter(singletonList(DokumentInfo.builder()
										.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM_3)
										.dokumentStatus(DOKUMENT_STATUS_3)
										.journalpostId(MOTTAKER_ID_3)
										.fagomradeCode(FAGOMRADE_CODE_3)
										.konversasjonId(KONVERSASJON_ID_3)
										.journalpostId(ARKIV_KODE_3)
										.build()))
								.build()
				)).build();
	}

	public static JiraSakResponseTo createJiraSakResponseTo() {
		return JiraSakResponseTo.builder()
				.jiraSakKey(AVSTEMT_REFERANSE)
				.message("https://jira.adeo.no/browse/MMA-1234")
				.build();
	}

	public static String convertDateTimeToString(OffsetDateTime offsetDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
		return offsetDateTime.format(formatter);

	}
}
