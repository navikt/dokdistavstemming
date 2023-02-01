package no.nav.dokdistavstemming.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OppdaterForsendelserAvstemtInfo {

    private String avstemtReferanse;
    private List<Forsendelse> forsendelser;
}
