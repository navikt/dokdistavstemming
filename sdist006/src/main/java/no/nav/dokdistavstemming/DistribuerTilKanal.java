//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package no.nav.dokdistavstemming;

import lombok.AllArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
		name = "DistribuerTilKanal",
		propOrder = {"forsendelseId"}
)
@XmlRootElement(
		namespace = "http://nav.no/melding/virksomhet/dokdistfordeling",
		name = "distribuerTilKanal"
)
@AllArgsConstructor
public class DistribuerTilKanal {
	@XmlElement(
			required = true
	)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(
			name = "token"
	)
	protected String forsendelseId;

	public DistribuerTilKanal() {
	}

	public String getForsendelseId() {
		return this.forsendelseId;
	}

	public void setForsendelseId(String value) {
		this.forsendelseId = value;
	}

	public DistribuerTilKanal withForsendelseId(String value) {
		this.setForsendelseId(value);
		return this;
	}
}
