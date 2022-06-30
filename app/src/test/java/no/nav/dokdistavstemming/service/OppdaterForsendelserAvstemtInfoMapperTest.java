package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.map.AvstemForsendelseMapper;
import no.nav.dokdistavstemming.domain.map.OppdaterForsendelserAvstemtInfoMapper;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokdistavstemming.utils.TestDataUtils.AVSTEMT_REFERANSE;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createDokDistAvstemmingRequestList;
import static no.nav.dokdistavstemming.utils.TestDataUtils.createJiraSakResponseTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

class OppdaterForsendelserAvstemtInfoMapperTest {

    private final OppdaterForsendelserAvstemtInfoMapper mapper = new OppdaterForsendelserAvstemtInfoMapper();
    private final AvstemForsendelseMapper avstemtInfoMapper = new AvstemForsendelseMapper();

    @Test
    public void shouldHentAvstemmingForsendelseResponse() {
        List<AvstemForsendelseResponseTo> avstemForsendelseResponseToList = createDokDistAvstemmingRequestList().stream()
                .map(avstemtInfoMapper::mapAvstemteForsendelser)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        OppdaterForsendelserAvstemtInfo forsendelserAvstemtInfo = mapper.map(avstemForsendelseResponseToList, createJiraSakResponseTo());
        assertOppdaterForsendelserAvstemtInfoMapper(forsendelserAvstemtInfo);
        assertThat(avstemForsendelseResponseToList.get(1).getForsendelseId(), is(forsendelserAvstemtInfo.getForsendelser().get(1).getForsendelseId()));
    }

    public void assertOppdaterForsendelserAvstemtInfoMapper(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {
        assertThat(oppdaterForsendelserAvstemtInfo.getAvstemtReferanse(), is(AVSTEMT_REFERANSE));
    }

}