package no.nav.dokdistavstemming.domain;

import no.nav.dokdistavstemming.domain.to.JiraSakResponseTo;

import java.util.ArrayList;
import java.util.List;

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
        List<OppdaterForsendelserAvstemtInfo.Forsendelse> forsendelser = new ArrayList<>();

        avstemForsendelseResponseToList.forEach(avstemForsendelseResponseTo -> {
            String forsendelseId = avstemForsendelseResponseTo.getForsendelseId();

            forsendelser.add(OppdaterForsendelserAvstemtInfo.Forsendelse.builder()
                    .forsendelseId(forsendelseId)
                    .build());

        });

        return forsendelser;
    }

}