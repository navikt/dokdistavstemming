package no.nav.dokdistavstemming.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Validated
@ConfigurationProperties("dokdistavstemming")
public class DokdistavstemmingProperties {

    private final Endpoints endpoints = new Endpoints();
    private final Sdist002Properties sdist002 = new Sdist002Properties();
    private final JiraUser jira = new JiraUser();
    private final Serviceuser serviceuser = new Serviceuser();
    private final Sdist004Properties sdist004 = new Sdist004Properties();

    @Data
    @Validated
    public static class Endpoints {
        @NotNull
        private AzureEndpoint dokarkiv;
    }

    @Data
    @Validated
    public static class AzureEndpoint {
        @NotEmpty
        private String url;
        @NotEmpty
        private String scope;
    }

    @Data
    @Validated
    public static class Sdist002Properties {
        @Min(1)
        private int delayTimePrint;
        @Min(1)
        private int delayTimeSDP;
        @Min(1)
        private int delayTimeEhandel;
    }

    @Data
    @Validated
    public static class Sdist004Properties {
        @Min(0)
        private int maxForsendelserRequest;
    }

    @Data
    @Validated
    public static class JiraUser {
        @NotEmpty
        private String url;
        @NotEmpty
        private String username;
        @NotEmpty
        private String password;
    }

    @Data
    @Validated
    public static class Serviceuser {
        @NotEmpty
        private String username;
        @NotEmpty
        private String password;
    }
}
