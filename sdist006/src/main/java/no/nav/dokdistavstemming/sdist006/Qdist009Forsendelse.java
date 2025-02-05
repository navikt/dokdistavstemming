package no.nav.dokdistavstemming.sdist006;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Builder;
import lombok.Data;

@XmlType(
		name = "Qdist009Forsendelse",
		propOrder = {"forsendelseId"}
)
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@Data
public class Qdist009Forsendelse {
	protected long forsendelseId;
}


