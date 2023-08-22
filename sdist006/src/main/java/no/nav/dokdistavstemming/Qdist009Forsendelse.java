package no.nav.dokdistavstemming;

import lombok.Builder;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

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


