package no.nav.dokdistavstemming.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OppdaterForsendelserAvstemtInfo {

    private String avstemtReferanse;
    private List<Forsendelse> forsendelser;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Forsendelse {
        private String forsendelseId;
    }

}
