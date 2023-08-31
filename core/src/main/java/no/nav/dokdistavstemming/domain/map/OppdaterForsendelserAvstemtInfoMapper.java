package no.nav.dokdistavstemming.domain.map;

import no.nav.dokdistavstemming.domain.Forsendelse;
import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelserAvstemtInfo;
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

	List<Forsendelse> mapForsendelseIder(List<UekspedertForsendelseDokument> uekspedertForsendelseDokumentList) {

		return uekspedertForsendelseDokumentList.stream()
				.map(uekspedertForsendelseDokument -> new Forsendelse(Long.valueOf(uekspedertForsendelseDokument.getForsendelseId())))
				.toList();
	}

}