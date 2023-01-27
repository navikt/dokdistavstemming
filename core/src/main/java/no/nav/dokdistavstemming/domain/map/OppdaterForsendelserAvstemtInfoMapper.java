package no.nav.dokdistavstemming.domain.map;

import no.nav.dokdistavstemming.domain.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;

import java.util.List;

public class OppdaterForsendelserAvstemtInfoMapper {

	public OppdaterForsendelserAvstemtInfo map(List<UekspedertForsendelseDokument> uekspedertForsendelseDokumentList, JiraSakResponseTo jiraSakResponseTo) {

		return OppdaterForsendelserAvstemtInfo.builder()
				.avstemtReferanse(jiraSakResponseTo.getJiraSakKey())
				.forsendelser(mapForsendelseIder(uekspedertForsendelseDokumentList))
				.build();
	}

	List<OppdaterForsendelserAvstemtInfo.Forsendelse> mapForsendelseIder(List<UekspedertForsendelseDokument> uekspedertForsendelseDokumentList) {

		return uekspedertForsendelseDokumentList.stream()
				.map(uekspedertForsendelseDokument -> OppdaterForsendelserAvstemtInfo.Forsendelse.builder()
						.forsendelseId(uekspedertForsendelseDokument.getForsendelseId())
						.build())
				.toList();
	}

}