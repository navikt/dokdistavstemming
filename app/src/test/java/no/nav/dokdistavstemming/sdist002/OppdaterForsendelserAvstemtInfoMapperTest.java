package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.map.OppdaterForsendelserAvstemtInfoMapper;
import no.nav.dokdistavstemming.domain.map.UekspedertForsendelseMapper;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static no.nav.dokdistavstemming.utils.TestDataUtils.AVSTEMT_REFERANSE;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createHentUekspederteForsendelserResponse;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createJiraSakResponseTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OppdaterForsendelserAvstemtInfoMapperTest {

    private final OppdaterForsendelserAvstemtInfoMapper oppdaterForsendelserAvstemtInfoMapper = new OppdaterForsendelserAvstemtInfoMapper();
    private final UekspedertForsendelseMapper uekspedertForsendelseMapper = new UekspedertForsendelseMapper();

    @Test
    public void shouldHentAvstemmingForsendelseResponse() {
        List<UekspedertForsendelseDokument> uekspedertForsendelseDokumentList = createHentUekspederteForsendelserResponse().getUekspederteForsendelser().stream()
                .map(uekspedertForsendelseMapper::mapUekspederteForsendelser)
                .flatMap(Collection::stream)
                .toList();

        OppdaterForsendelserAvstemtInfo forsendelserAvstemtInfo = oppdaterForsendelserAvstemtInfoMapper.map(uekspedertForsendelseDokumentList, createJiraSakResponseTo());
        assertOppdaterForsendelserAvstemtInfoMapper(forsendelserAvstemtInfo);
        assertThat(Long.valueOf(uekspedertForsendelseDokumentList.get(1).getForsendelseId()), is(forsendelserAvstemtInfo.getForsendelser().get(1).getForsendelseId()));
    }

    public void assertOppdaterForsendelserAvstemtInfoMapper(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {
        assertThat(oppdaterForsendelserAvstemtInfo.getAvstemtReferanse(), is(AVSTEMT_REFERANSE));
    }

}