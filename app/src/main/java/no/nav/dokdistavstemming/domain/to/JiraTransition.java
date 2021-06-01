package no.nav.dokdistavstemming.domain.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraTransition {

    private Transition transition;

    @Data
    @Builder
    public static class Transition {
        private String id;

    }

}
