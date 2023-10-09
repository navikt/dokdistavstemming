package no.nav.dokdistavstemming.domain.to;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraTransition {

    @NotNull
    private Transition transition;

    @Data
    @Builder
    public static class Transition {
        private String id;

    }

}
