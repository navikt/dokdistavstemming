package no.nav.dokdistavstemming.domain.map;

import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
import no.nav.dokdistavstemming.domain.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */
public class OppdaterForsendelserAvstemtInfoMapper {


    public OppdaterForsendelserAvstemtInfo map(List<AvstemForsendelseResponseTo> avstemForsendelseResponseToList, JiraSakResponseTo jiraSakResponseTo) {

        return OppdaterForsendelserAvstemtInfo.builder()
                .avstemtReferanse(jiraSakResponseTo.getJiraSakKey())
                .forsendelser(mapForsendelseIder(avstemForsendelseResponseToList))
                .build();
    }

    List<OppdaterForsendelserAvstemtInfo.Forsendelse> mapForsendelseIder(List<AvstemForsendelseResponseTo> avstemForsendelseResponseToList) {

        return avstemForsendelseResponseToList.stream()
                .map(avstemForsendelseResponseTo -> OppdaterForsendelserAvstemtInfo.Forsendelse.builder()
                        .forsendelseId(avstemForsendelseResponseTo.getForsendelseId())
                        .build())
                .collect(Collectors.toList());

    }

}