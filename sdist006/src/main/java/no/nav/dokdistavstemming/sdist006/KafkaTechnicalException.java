package no.nav.dokdistavstemming.sdist006;

import no.nav.dokdistavstemming.exceptions.DokdistavstemmingTechnicalException;

public class KafkaTechnicalException extends DokdistavstemmingTechnicalException {
	public KafkaTechnicalException(String s, Throwable t) {
		super(s, t);
	}
}
