package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostWithDistribusjonsinfo;
import no.nav.dokdistavstemming.domain.Digitalpostkasse;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.domain.DittNavVarsel;
import no.nav.dokdistavstemming.domain.EkspedertForsendelse;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.domain.PostadresseTo;
import no.nav.dokdistavstemming.domain.UtsendingsKanalCode;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.DITTNAV;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.utils.ConverterUtils.convertStringToDateTime;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BulkOppdaterDistribusjonsinfoMapper {

	public BulkOppdaterDistribusjonsinfoRequest map(HentEkspederteForsendelserResponse hentEkspederteForsendelser) {
		if (hentEkspederteForsendelser.getForsendelser() == null || hentEkspederteForsendelser.getForsendelser().isEmpty()) {
			return null;
		}

		List<JournalpostWithDistribusjonsinfo> journalpostWithDistribusjonsinfos = hentEkspederteForsendelser.getForsendelser().stream()
				.filter(ekspederteForsendelse -> isNotBlank(ekspederteForsendelse.getJournalpostId()))
				.filter(this::isPostadresseDigitalPostInfoOgVarselNonNull)
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
				.varsel(mapDittNavVarsel(ekspederteForsendelse.getVarsel(), ekspederteForsendelse.getDistribusjonsKanal()))
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
						.landkode(postadresse.getLandkode())
						.build() : null;
	}

	private Digitalpostkasse mapDigitalpostkasse(Digitalpostkasse digitalpostkasse, String kanal) {
		return SDP.name().equals(kanal) && digitalpostkasse != null ?
				Digitalpostkasse.builder()
						.digitalpostkasseadresse(digitalpostkasse.getDigitalpostkasseadresse())
						.digitalpostkasseleverandor(digitalpostkasse.getDigitalpostkasseleverandor())
						.build() : null;

	}

	private DittNavVarsel mapDittNavVarsel(DittNavVarsel dittNavVarsel, String kanal) {
		return DITTNAV.name().equals(kanal) && dittNavVarsel != null ?
				DittNavVarsel.builder()
						.varseltekst(dittNavVarsel.getVarseltekst())
						.digitalkontaktinformasjon(dittNavVarsel.getDigitalkontaktinformasjon())
						.build() : null;

	}

	private String mapUtsendingsKanalCode(String distKanal) {
		DistribusjonKanalCode distribusjonKanalCode = DistribusjonKanalCode.valueOf(distKanal);
		switch (distribusjonKanalCode) {
			case SDP -> {
				return UtsendingsKanalCode.SDP.name();
			}
			case DITTNAV -> {
				return UtsendingsKanalCode.NAV_NO.name();
			}
			case PRINT -> {
				return UtsendingsKanalCode.S.name();
			}
			default -> {
				return null;
			}
		}
	}

	private boolean isPostadresseDigitalPostInfoOgVarselNonNull(EkspedertForsendelse ekspederteForsendelse) {
		return nonNull(ekspederteForsendelse.getPostadresse()) || nonNull(ekspederteForsendelse.getDigitalpostkasse())
				|| nonNull(ekspederteForsendelse.getVarsel());
	}
}
