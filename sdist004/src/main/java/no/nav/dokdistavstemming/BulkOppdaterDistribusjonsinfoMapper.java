package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.dokdistadmin.to.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostWithDistribusjonsinfo;
import no.nav.dokdistavstemming.consumer.journalpostapi.to.EpostvarselTo;
import no.nav.dokdistavstemming.consumer.journalpostapi.to.SmsvarselTo;
import no.nav.dokdistavstemming.consumer.journalpostapi.to.VarselTo;
import no.nav.dokdistavstemming.domain.Digitalpostkasse;
import no.nav.dokdistavstemming.domain.EkspedertForsendelse;
import no.nav.dokdistavstemming.domain.Epostvarsel;
import no.nav.dokdistavstemming.domain.PostadresseTo;
import no.nav.dokdistavstemming.domain.Smsvarsel;
import no.nav.dokdistavstemming.domain.Varsel;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.enums.UtsendingsKanalCode;

import java.util.List;
import java.util.Objects;

import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.DITTNAV;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode.valueOf;
import static no.nav.dokdistavstemming.utils.ConverterUtils.convertStringToDateTime;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BulkOppdaterDistribusjonsinfoMapper {

	private static final String UNKNOWN_ALPHA3_LANDKODE = "???";
	private static final String UNKNOWN_ALPHA2_LANDKODE = "??";

	public BulkOppdaterDistribusjonsinfoRequest map(HentEkspederteForsendelserResponse hentEkspederteForsendelser) {
		if (hentEkspederteForsendelser.getForsendelser() == null || hentEkspederteForsendelser.getForsendelser().isEmpty()) {
			return null;
		}

		List<JournalpostWithDistribusjonsinfo> journalpostWithDistribusjonsinfos = hentEkspederteForsendelser.getForsendelser().stream()
				.filter(ekspederteForsendelse -> isNotBlank(ekspederteForsendelse.getJournalpostId()))
				.map(this::journalpostWithDistribusjonsinfo)
				.filter(Objects::nonNull)
				.toList();
		return BulkOppdaterDistribusjonsinfoRequest.builder()
				.journalposter(journalpostWithDistribusjonsinfos)
				.build();
	}

	private JournalpostWithDistribusjonsinfo journalpostWithDistribusjonsinfo(EkspedertForsendelse ekspederteForsendelse) {
		return JournalpostWithDistribusjonsinfo.builder()
				.journalpostId(Long.valueOf(ekspederteForsendelse.getJournalpostId()))
				.forsendelseId(ekspederteForsendelse.getForsendelseId())
				.ekspedertDato(convertStringToDateTime(ekspederteForsendelse.getEkspedertDato()))
				.utsendingsKanal(mapUtsendingsKanalCode(ekspederteForsendelse.getDistribusjonsKanal()))
				.settStatusEkspedert(true)
				.digitalpostkasse(mapDigitalpostkasse(ekspederteForsendelse.getDigitalpostkasse(), ekspederteForsendelse.getDistribusjonsKanal()))
				.postadresse(mapPostadresse(ekspederteForsendelse.getPostadresse(), ekspederteForsendelse.getDistribusjonsKanal()))
				.varsel(mapVarsel(ekspederteForsendelse.getVarsel(), ekspederteForsendelse.getDistribusjonsKanal()))
				.build();
	}

	private PostadresseTo mapPostadresse(PostadresseTo postadresse, String kanal) {
		return PRINT.name().equals(kanal) && postadresse != null ?
				PostadresseTo.builder()
						.adresselinje1(postadresse.getAdresselinje1())
						.adresselinje2(postadresse.getAdresselinje2())
						.adresselinje3(postadresse.getAdresselinje3())
						.postnummer(postadresse.getPostnummer())
						.poststed(postadresse.getPoststed())
						.landkode(UNKNOWN_ALPHA3_LANDKODE.equals(postadresse.getLandkode()) ? UNKNOWN_ALPHA2_LANDKODE : postadresse.getLandkode())
						.build() : null;
	}

	private Digitalpostkasse mapDigitalpostkasse(Digitalpostkasse digitalpostkasse, String kanal) {
		return SDP.name().equals(kanal) && digitalpostkasse != null ?
				Digitalpostkasse.builder()
						.digitalpostkasseadresse(digitalpostkasse.getDigitalpostkasseadresse())
						.digitalpostkasseleverandor(digitalpostkasse.getDigitalpostkasseleverandor())
						.build() : null;

	}

	private VarselTo mapVarsel(Varsel varsel, String kanal) {
		if (varsel != null && (DITTNAV.name().equals(kanal) || SDP.name().equals(kanal)))
			return new VarselTo(mapEpostVarsler(varsel.getEpostvarsel()), mapSmsVarsler(varsel.getSmsvarsel()));

		return null;
	}

	private List<EpostvarselTo> mapEpostVarsler(List<Epostvarsel> epostvarsler) {
		if (epostvarsler == null) {
			return null;
		}

		return epostvarsler.stream()
				.map(EpostvarselTo::fromEpostvarsel)
				.toList();
	}

	private List<SmsvarselTo> mapSmsVarsler(List<Smsvarsel> smsvarsler) {
		if (smsvarsler == null) {
			return null;
		}

		return smsvarsler.stream()
				.map(SmsvarselTo::fromSmsvarsel)
				.toList();
	}
	private String mapUtsendingsKanalCode(String distKanal) {
		DistribusjonKanalCode distribusjonKanalCode = valueOf(distKanal);
		return switch (distribusjonKanalCode) {
			case SDP -> UtsendingsKanalCode.SDP.name();
			case DITTNAV -> UtsendingsKanalCode.NAV_NO.name();
			case PRINT -> UtsendingsKanalCode.S.name();
			case TRYGDERETTEN -> UtsendingsKanalCode.TRYGDERETTEN.name();
			case DPVT -> UtsendingsKanalCode.DPVT.name();
			default -> null;
		};
	}
}
