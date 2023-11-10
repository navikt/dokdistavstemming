package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.consumer.dokdistadmin.to.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.map.OppdaterForsendelserAvstemtInfoMapper;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OppdaterForsendelserAvstemtInfoMapperTest {

	private final OppdaterForsendelserAvstemtInfoMapper oppdaterForsendelserAvstemtInfoMapper = new OppdaterForsendelserAvstemtInfoMapper();
	private final UekspedertForsendelseMapper uekspedertForsendelseMapper = new UekspedertForsendelseMapper();

	@Test
	public void shouldHentAvstemmingForsendelseResponse() {
		List<UekspedertForsendelseDokument> uekspedertForsendelseDokumentList = TestDataUtils.createHentUekspederteForsendelserResponse().getUekspederteForsendelser().stream()
				.map(uekspedertForsendelseMapper::mapUekspederteForsendelser)
				.flatMap(Collection::stream)
				.toList();

		OppdaterForsendelserAvstemtInfo forsendelserAvstemtInfo = oppdaterForsendelserAvstemtInfoMapper.map(uekspedertForsendelseDokumentList, TestDataUtils.createJiraSakResponseTo());
		assertOppdaterForsendelserAvstemtInfoMapper(forsendelserAvstemtInfo);
		assertThat(Long.valueOf(uekspedertForsendelseDokumentList.get(1).getForsendelseId()), is(forsendelserAvstemtInfo.getForsendelser().get(1).getForsendelseId()));
	}

	public void assertOppdaterForsendelserAvstemtInfoMapper(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {
		assertThat(oppdaterForsendelserAvstemtInfo.getAvstemtReferanse(), Matchers.is(TestDataUtils.AVSTEMT_REFERANSE));
	}

}