package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.map.OppdaterForsendelserAvstemtInfoMapper;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static no.nav.dokdistavstemming.sdist002.TestDataUtils.AVSTEMT_REFERANSE;
import static no.nav.dokdistavstemming.sdist002.TestDataUtils.createHentUekspederteForsendelserResponse;
import static org.assertj.core.api.Assertions.assertThat;

class OppdaterForsendelserAvstemtInfoMapperTest {

	private final OppdaterForsendelserAvstemtInfoMapper oppdaterForsendelserAvstemtInfoMapper = new OppdaterForsendelserAvstemtInfoMapper();
	private final UekspedertForsendelseMapper uekspedertForsendelseMapper = new UekspedertForsendelseMapper();

	@Test
	public void shouldHentAvstemmingForsendelseResponse() {
		List<UekspedertForsendelseDokument> uekspedertForsendelseDokumentList = createHentUekspederteForsendelserResponse().getUekspederteForsendelser().stream()
				.map(uekspedertForsendelseMapper::mapUekspederteForsendelser)
				.flatMap(Collection::stream)
				.toList();

		OppdaterForsendelserAvstemtInfo forsendelserAvstemtInfo = oppdaterForsendelserAvstemtInfoMapper.map(uekspedertForsendelseDokumentList, TestDataUtils.createJiraSakResponseTo());

		assertOppdaterForsendelserAvstemtInfoMapper(forsendelserAvstemtInfo);
		assertThat(uekspedertForsendelseDokumentList.get(1).forsendelseId()).isEqualTo(forsendelserAvstemtInfo.getForsendelser().get(1).getForsendelseId());
	}

	public void assertOppdaterForsendelserAvstemtInfoMapper(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {
		assertThat(oppdaterForsendelserAvstemtInfo.getAvstemtReferanse()).isEqualTo(AVSTEMT_REFERANSE);
	}

}