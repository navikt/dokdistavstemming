package no.nav.dokdistavstemming.sdist002;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record Project(
		String self,
		String id,
		String key,
		String name,
		String description) {
}
