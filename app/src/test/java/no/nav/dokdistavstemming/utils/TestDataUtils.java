package no.nav.dokdistavstemming.utils;

import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TestDataUtils {


	public static final Long DOKUMENTINFO_ID = 1110L;
	public static final Long DISTRIBUSJONINFO_ID = 1111L;
	public static final String KONVERSASJON_ID = "7ef3e7c7-cd4c-40bd-a5bf-99c5dbb26131";
	public static final String DISTRIBUSJON_ID = "7882d37e-34f7-11e9-b677-d663bd953d61";
	public static final String BESTILLENDE_FAGSYSTEM = "ARENA";
	public static final String FAGOMRADE_CODE = "DAG";
	public static final LocalDateTime PRODUKSJON_DATO = LocalDateTime.now().minusDays(6).minusMinutes(23);
	public static final LocalDateTime EKSPEDERT_DATO = LocalDateTime.now();
	public static final LocalDateTime DISTRIBUSJON_DATO = LocalDateTime.now().minusDays(7).minusHours(23).minusMinutes(59).minusSeconds(59);
	public static final String MOTTAKER_ID = "***gammelt_fnr***";
	public static final String DIGITAL_DISTRIBUTOR_ID = "996460320";
	public static final String ARKIV_KODE = "389426100";
	public static final String DOKUMENT_STATUS = "OPPRETTET";
	public static final String DISTRIBUSJON_STATUS = "OPPRETTET";
	public static final DistribusjonKanalCode DISTRIBUSJON_KANAL = DistribusjonKanalCode.PRINT;
	public static final String EPOSTADDRESS = "epostaddress0@nav.no";
	public static final Long VARSELID = 2000L;


	public static final Long DOKUMENTINFO_ID_1 = 1111L;
	public static final Long DISTRIBUSJONINFO_ID_1 = 1111L;
	public static final String KONVERSASJON_ID_1 = "7ef3e7c7-cd4c-40bd-a5bf-99c5dbb26131";
	public static final String DISTRIBUSJON_ID_1 = "7882d37e-34f7-11e9-b677-d663bd953d61";
	public static final String BESTILLENDE_FAGSYSTEM_1 = "ARENA";
	public static final LocalDateTime PRODUKSJON_DATO_1 = LocalDateTime.now().minusDays(6).minusMinutes(23);
	public static final LocalDateTime EKSPEDERT_DATO_1 = LocalDateTime.now();
	public static final LocalDateTime DISTRIBUSJON_DATO_1 = LocalDateTime.now().minusDays(7).minusHours(23).minusMinutes(59).minusSeconds(59);
	public static final String MOTTAKER_ID_1 = "***gammelt_fnr***";
	public static final String DIGITAL_DISTRIBUTOR_ID_1 = "996460321";
	public static final String ARKIV_KODE_1 = "389426111";
	public static final String DOKUMENT_STATUS_1 = "OPPRETTET";
	public static final String DISTRIBUSJON_STATUS_1_ = "OPPRETTET";
	public static final DistribusjonKanalCode DISTRIBUSJON_KANAL_1 = DistribusjonKanalCode.PRINT;
	public static final Long VARSELID_1 = 2111L;
	public static final String EPOSTADDRESS_1 = "epostaddress1@nav.no";

	//SDP forsinket
	public static final Long DOKUMENTINFO_ID_2 = 1222L;
	public static final Long DISTRIBUSJONINFO_ID_2 = 1222L;
	public static final String KONVERSASJON_ID_2 = "7ef3e7c7-cd4c-40bd-a5bf-99c5dbb26132";
	public static final String DISTRIBUSJON_ID_2 = "7882d37e-34f7-11e9-b677-d663bd953d62";
	public static final String BESTILLENDE_FAGSYSTEM_2 = "ARENA";
	public static final String FAGOMRADE_CODE_2 = "DAG";
	public static final LocalDateTime PRODUKSJON_DATO_2 = LocalDateTime.now().minusDays(6).minusMinutes(23);
	public static final LocalDateTime DISTRIBUSJON_DATO_2 = LocalDateTime.now().minusHours(6).minusSeconds(59);
	public static final String ARKIV_KODE_2 = "389426102";
	public static final String MOTTAKER_ID_2 = "***gammelt_fnr***";
	public static final String DOKUMENT_STATUS_2 = "OPPRETTET";
	public static final String DISTRIBUSJON_STATUS_2 = "OPPRETTET";
	public static final DistribusjonKanalCode DISTRIBUSJON_KANAL_2 = DistribusjonKanalCode.SDP;
	public static final Long VARSELID_2 = 2222L;
	public static final String EPOSTADDRESS_2 = "epostaddress2@nav.no";


	public static final Long DOKUMENTINFO_ID_3 = 1333L;
	public static final Long DISTRIBUSJONINFO_ID_3 = 1333L;
	public static final String KONVERSASJON_ID_3 = "7ef3e7c7-cd4c-40bd-a5bf-99c5dbb26143";
	public static final String DISTRIBUSJON_ID_3 = "7882d37e-34f7-11e9-b677-d663bd953d63";
	public static final String BESTILLENDE_FAGSYSTEM_3 = "ARENA";
	public static final String FAGOMRADE_CODE_3 = "AAP";
	public static final LocalDateTime OPPRETTET_DATO_3 = LocalDateTime.now().minusHours(6).minusSeconds(3);
	public static final LocalDateTime DISTRIBUSJON_DATO_3 = LocalDateTime.now().minusHours(6).minusMinutes(1).minusSeconds(59);
	public static final String MOTTAKER_ID_3 = "***gammelt_fnr***";
	public static final String ARKIV_KODE_3 = "389426113";
	public static final String DIGITAL_DISTRIBUTOR_ID_3 = "984661183";
	public static final String DOKUMENT_STATUS_3 = "OVERSENDT";
	public static final String DISTRIBUSJON_STATUS_3 = "OVERSENDT";
	public static final DistribusjonKanalCode DISTRIBUSJON_KANAL_3 = DistribusjonKanalCode.SDP;
	public static final Long VARSELID_3 = 2333L;
	public static final String EPOSTADDRESS_3 = "epostaddress3@nav.no";


	private static String DIGITAL_DISTRIBUTOR_ID_2 = "984661183";

	public static List<HentUekspederForsendelseResponseTo> createDokDistAvstemmingForsendelses() {
		return Arrays.asList(HentUekspederForsendelseResponseTo.builder()
						.forsendelseId(DISTRIBUSJON_ID)
						.distribusjonKanal(DISTRIBUSJON_KANAL)
						.distribusjonStatus(DISTRIBUSJON_STATUS)
						.produksjonDato(PRODUKSJON_DATO)
						.distribusjonDato(DISTRIBUSJON_DATO)
						.dokumenter(Arrays.asList(HentUekspederForsendelseResponseTo.DokumentInfoTo.builder()
										.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
										.dokumentStatus(DOKUMENT_STATUS)
										.mottakkerId(MOTTAKER_ID)
										.konversasjonId(KONVERSASJON_ID)
										.arkivKode(ARKIV_KODE)
										.fagomradeCode(FAGOMRADE_CODE)
										.digitalDistributorId(DIGITAL_DISTRIBUTOR_ID)
										.build()))
						.build(),
				HentUekspederForsendelseResponseTo.builder().forsendelseId(DISTRIBUSJON_ID_2)
						.distribusjonKanal(DISTRIBUSJON_KANAL_2)
						.distribusjonStatus(DISTRIBUSJON_STATUS_2)
						.produksjonDato(PRODUKSJON_DATO_2)
						.distribusjonDato(DISTRIBUSJON_DATO_2)
						.dokumenter(Arrays.asList(HentUekspederForsendelseResponseTo.DokumentInfoTo.builder()
								.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM_2)
								.dokumentStatus(DOKUMENT_STATUS_2)
								.mottakkerId(MOTTAKER_ID_2)
								.fagomradeCode(FAGOMRADE_CODE_2)
								.konversasjonId(KONVERSASJON_ID_2)
								.arkivKode(ARKIV_KODE_2)
								.digitalDistributorId(DIGITAL_DISTRIBUTOR_ID_2)
								.build()))
						.build(),
				HentUekspederForsendelseResponseTo.builder()
						.forsendelseId(DISTRIBUSJON_ID_3)
						.distribusjonKanal(DISTRIBUSJON_KANAL_3)
						.distribusjonStatus(DISTRIBUSJON_STATUS_3)
						.produksjonDato(OPPRETTET_DATO_3)
						.distribusjonDato(DISTRIBUSJON_DATO_3)
						.countDokument(1L)
						.dokumenter(Arrays.asList(HentUekspederForsendelseResponseTo.DokumentInfoTo.builder()
								.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM_3)
								.dokumentStatus(DOKUMENT_STATUS_3)
								.mottakkerId(MOTTAKER_ID_3)
								.fagomradeCode(FAGOMRADE_CODE_3)
								.konversasjonId(KONVERSASJON_ID_3)
								.arkivKode(ARKIV_KODE_3)
								.digitalDistributorId(DIGITAL_DISTRIBUTOR_ID_3)
								.build()))
						.build());
	}

	public static HentUekspederForsendelseResponseTo createDokDistAvstemmingForsendels() {
		return HentUekspederForsendelseResponseTo.builder()
				.forsendelseId(DISTRIBUSJON_ID)
				.distribusjonKanal(DISTRIBUSJON_KANAL)
				.distribusjonStatus(DISTRIBUSJON_STATUS)
				.produksjonDato(PRODUKSJON_DATO)
				.distribusjonDato(DISTRIBUSJON_DATO)
				.countDokument(1L)
				.dokumenter(Arrays.asList(HentUekspederForsendelseResponseTo.DokumentInfoTo.builder()
						.bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
						.dokumentStatus(DOKUMENT_STATUS)
						.mottakkerId(MOTTAKER_ID)
						.konversasjonId(KONVERSASJON_ID)
						.arkivKode(ARKIV_KODE)
						.fagomradeCode(FAGOMRADE_CODE)
						.digitalDistributorId(DIGITAL_DISTRIBUTOR_ID)
						.build()))
				.build();
	}

}
