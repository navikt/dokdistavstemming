package no.nav.dokdistavstemming.domain.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Field {
	private Boolean required;
	private Schema schema;
	private String name;
	private Boolean hasDefaultValue;
	private Set<String> operations;
	private List<AllowedValue> allowedValues;

	public List<AllowedValue> getAllowedValues() {
		if (allowedValues == null) {
			allowedValues = new ArrayList<>();
		}
		return allowedValues;
	}
}
