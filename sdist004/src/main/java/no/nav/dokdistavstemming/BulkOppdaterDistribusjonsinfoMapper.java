package no.nav.dokdistavstemming;

import no.nav.dokdistavstemming.consumer.journalpostapi.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokdistavstemming.consumer.journalpostapi.JournalpostWithDistribusjonsinfo;
import no.nav.dokdistavstemming.domain.Digitalpostkasse;
import no.nav.dokdistavstemming.domain.DittNavVarsel;
import no.nav.dokdistavstemming.domain.EkspederteForsendelse;
import no.nav.dokdistavstemming.domain.HentEkspederteForsendelserResponse;
import no.nav.dokdistavstemming.domain.PostadresseTo;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.DITTNAV;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.PRINT;
import static no.nav.dokdistavstemming.domain.DistribusjonKanalCode.SDP;
import static no.nav.dokdistavstemming.utils.ConverterUtils.convertStringToDateTime;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BulkOppdaterDistribusjonsinfoMapper {

	public BulkOppdaterDistribusjonsinfoRequest map(HentEkspederteForsendelserResponse hentEkspederteForsendelser) {
		List<JournalpostWithDistribusjonsinfo> journalpostWithDistribusjonsinfos = hentEkspederteForsendelser.getForsendelser().stream()
				.filter(ekspederteForsendelse -> isNotBlank(ekspederteForsendelse.getJournalpostId()))
				.filter(this::isPostadresseDigitalPostInfoOgVarselNonNull)
				.map(this::journalpostWithDistribusjonsinfo)
				.collect(Collectors.toList());
		return BulkOppdaterDistribusjonsinfoRequest.builder()
				.journalposter(journalpostWithDistribusjonsinfos)
				.build();
	}

	private JournalpostWithDistribusjonsinfo journalpostWithDistribusjonsinfo(EkspederteForsendelse ekspederteForsendelse) {
		return JournalpostWithDistribusjonsinfo.builder()
				.journalpostId(Long.valueOf(ekspederteForsendelse.getJournalpostId()))
				.forsendelseId(ekspederteForsendelse.getForsendelseId())
				.ekspedertDato(convertStringToDateTime(ekspederteForsendelse.getEkspedertDato()))
				.utsendingsKanal(ekspederteForsendelse.getDistribusjonsKanal())
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

	private boolean isPostadresseDigitalPostInfoOgVarselNonNull(EkspederteForsendelse ekspederteForsendelse) {
		return nonNull(ekspederteForsendelse.getPostadresse()) || nonNull(ekspederteForsendelse.getDigitalpostkasse())
				|| nonNull(ekspederteForsendelse.getVarsel());
	}
}
