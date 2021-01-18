package no.nav.dokdistavstemming.config;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.Min;

@Getter
@Setter
@Configuration
@ConfigurationProperties(value = "dokdistavstemming")
@AllArgsConstructor
@NoArgsConstructor
public class DokdistavstemmingProp {

    @Min(1)
    private int delayTimePrint;
    @Min(1)
    private int delayTimeSDP;

}
