package no.nav.dokdistavstemming.utils;

import com.pep1.jira.client.domain.issue.Attachment;
import com.pep1.jira.client.domain.issue.Issue;
import com.pep1.jira.client.domain.issue.IssueFields;
import com.pep1.jira.client.domain.issue.IssueType;
import com.pep1.jira.client.domain.issue.Priority;
import com.pep1.jira.client.domain.issue.Reporter;
import com.pep1.jira.client.domain.issue.request.IssueInput;
import com.pep1.jira.client.domain.project.Project;
import no.nav.dokdistavstemming.domain.AvstemForsendelseRequestTo;
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static no.nav.dokdistavstemming.utils.TestUtils.convertDateTimeToString;

public class TestDataUtils {


    public static final String KONVERSASJON_ID = "7ef3e7c7-cd4c-40bd-a5bf-99c5dbb26131";
    public static final String DISTRIBUSJON_ID = "7882d37e-34f7-11e9-b677-d663bd953d61";
    public static final String BESTILLENDE_FAGSYSTEM = "ARENA";
    public static final String FAGOMRADE_CODE = "DAG";
    public static final String PRODUKSJON_DATO = convertDateTimeToString(LocalDateTime.now().minusDays(6).minusMinutes(23));
    public static final String EKSPEDERT_DATO = convertDateTimeToString(LocalDateTime.now());
    public static final String DISTRIBUSJON_DATO = convertDateTimeToString(LocalDateTime.now().minusDays(7).minusHours(23).minusMinutes(59));
    public static final String MOTTAKER_ID = "***gammelt_fnr***";
    public static final String DIGITAL_DISTRIBUTOR_ID = "996460320";
    public static final String ARKIV_KODE = "389426100";
    public static final String DOKUMENT_STATUS = "OPPRETTET";
    public static final String DISTRIBUSJON_STATUS = "OPPRETTET";
    public static final DistribusjonKanalCode DISTRIBUSJON_KANAL = DistribusjonKanalCode.PRINT;
    public static final DistribusjonKanalCode DISTRIBUSJON_KANAL_1 = DistribusjonKanalCode.PRINT;

    public static final String KONVERSASJON_ID_2 = "7ef3e7c7-cd4c-40bd-a5bf-99c5dbb26132";
    public static final String DISTRIBUSJON_ID_2 = "7882d37e-34f7-11e9-b677-d663bd953d62";
    public static final String BESTILLENDE_FAGSYSTEM_2 = "ARENA";
    public static final String FAGOMRADE_CODE_2 = "DAG";
    public static final String PRODUKSJON_DATO_2 = convertDateTimeToString(LocalDateTime.now().minusDays(6).minusMinutes(23));
    public static final String DISTRIBUSJON_DATO_2 = convertDateTimeToString(LocalDateTime.now().minusHours(6).minusSeconds(59));
    public static final String ARKIV_KODE_2 = "389426102";
    public static final String MOTTAKER_ID_2 = "***gammelt_fnr***";
    public static final String DOKUMENT_STATUS_2 = "OPPRETTET";
    public static final String DISTRIBUSJON_STATUS_2 = "OPPRETTET";
    public static final DistribusjonKanalCode DISTRIBUSJON_KANAL_2 = DistribusjonKanalCode.SDP;
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
    public static final String SAK_ID = "533815";
    public static final String KEY = "MMA-43";
    public static final String SELF = "https://jira-q1.adeo.no/rest/api/2/issue/533815";
    public static String DISTRIBUSJON_ID_PRINT = "c3bc9d18-c5b8-40d2-9d50-5cbfa661475c";
    public static String FORSENDELSE_ID_PRINT = "5267785";
    public static String DISTRIBUSJON_ID_SDP = "39df91f9-173b-4bf4-89ec-2a9577f227fb";
    public static String DISTRIBUSJON_STATUS_J = "OVERSENDT";
    public static String DISRIBUSJON_DATO_J = "2020-01-14 13:00:48";
    public static DistribusjonKanalCode DISTRIBUSJON_KANAL_P_J = DistribusjonKanalCode.PRINT;

    public static List<AvstemForsendelseRequestTo> createDokDistAvstemmingRequestList() {
        return Arrays.asList(AvstemForsendelseRequestTo.builder()
                        .distribusjonId(DISTRIBUSJON_ID)
                        .distribusjonKanal(DISTRIBUSJON_KANAL.name())
                        .distribusjonStatus(DISTRIBUSJON_STATUS)
                        .opprettetDato(PRODUKSJON_DATO)
                        .distribusjonDato(DISTRIBUSJON_DATO)
                        .dokumenter(Arrays.asList(AvstemForsendelseRequestTo.DokumentInfoTo.builder()
                                .bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
                                .dokumentStatus(DOKUMENT_STATUS)
                                .konversasjonId(KONVERSASJON_ID)
                                .journalpostId(ARKIV_KODE)
                                .fagomradeCode(FAGOMRADE_CODE)
                                .build()))
                        .build(),
                AvstemForsendelseRequestTo.builder().distribusjonId(DISTRIBUSJON_ID_2)
                        .distribusjonKanal(DISTRIBUSJON_KANAL_2.name())
                        .distribusjonStatus(DISTRIBUSJON_STATUS_2)
                        .opprettetDato(PRODUKSJON_DATO_2)
                        .distribusjonDato(DISTRIBUSJON_DATO_2)
                        .dokumenter(Arrays.asList(AvstemForsendelseRequestTo.DokumentInfoTo.builder()
                                .bestillendeFagsystem(BESTILLENDE_FAGSYSTEM_2)
                                .dokumentStatus(DOKUMENT_STATUS_2)
                                .fagomradeCode(FAGOMRADE_CODE_2)
                                .konversasjonId(KONVERSASJON_ID_2)
                                .journalpostId(ARKIV_KODE_2)
                                .build()))
                        .build(),
                AvstemForsendelseRequestTo.builder()
                        .distribusjonId(DISTRIBUSJON_ID_3)
                        .distribusjonKanal(DISTRIBUSJON_KANAL_3.name())
                        .distribusjonStatus(DISTRIBUSJON_STATUS_3)
                        .opprettetDato(convertDateTimeToString(OPPRETTET_DATO_3))
                        .distribusjonDato(convertDateTimeToString(DISTRIBUSJON_DATO_3))
                        .dokumenter(Arrays.asList(AvstemForsendelseRequestTo.DokumentInfoTo.builder()
                                .bestillendeFagsystem(BESTILLENDE_FAGSYSTEM_3)
                                .dokumentStatus(DOKUMENT_STATUS_3)
                                .journalpostId(MOTTAKER_ID_3)
                                .fagomradeCode(FAGOMRADE_CODE_3)
                                .konversasjonId(KONVERSASJON_ID_3)
                                .journalpostId(ARKIV_KODE_3)
                                .build()))
                        .build());
    }

    public static AvstemForsendelseRequestTo createDokDistAvstemmingRequestTo() {
        return AvstemForsendelseRequestTo.builder()
                .distribusjonId(DISTRIBUSJON_ID)
                .distribusjonKanal(DISTRIBUSJON_KANAL.name())
                .distribusjonStatus(DISTRIBUSJON_STATUS)
                .opprettetDato(PRODUKSJON_DATO)
                .distribusjonDato(DISTRIBUSJON_DATO)
                .dokumenter(Arrays.asList(AvstemForsendelseRequestTo.DokumentInfoTo.builder()
                        .bestillendeFagsystem(BESTILLENDE_FAGSYSTEM)
                        .dokumentStatus(DOKUMENT_STATUS)
                        .journalpostId(MOTTAKER_ID)
                        .konversasjonId(KONVERSASJON_ID)
                        .journalpostId(ARKIV_KODE)
                        .fagomradeCode(FAGOMRADE_CODE)
                        .build()))
                .build();
    }

    public static List<AvstemForsendelseRequestTo> createDokDistAvstemmingSDPRequestTo() {
        return Arrays.asList(AvstemForsendelseRequestTo.builder()
                .distribusjonId(DISTRIBUSJON_ID_3)
                .distribusjonKanal(DISTRIBUSJON_KANAL_3.name())
                .distribusjonStatus(DISTRIBUSJON_STATUS_3)
                .opprettetDato(convertDateTimeToString(OPPRETTET_DATO_3))
                .distribusjonDato(convertDateTimeToString(DISTRIBUSJON_DATO_3))
                .dokumenter(Arrays.asList(AvstemForsendelseRequestTo.DokumentInfoTo.builder()
                        .bestillendeFagsystem(BESTILLENDE_FAGSYSTEM_3)
                        .dokumentStatus(DOKUMENT_STATUS_3)
                        .journalpostId(MOTTAKER_ID_3)
                        .fagomradeCode(FAGOMRADE_CODE_3)
                        .konversasjonId(KONVERSASJON_ID_3)
                        .journalpostId(ARKIV_KODE_3)
                        .build()))
                .build());
    }

}
