package no.nav.dokdistavstemming.Exceptions;

import no.nav.dokdistavstemming.exceptions.DokdistavstemmingTechnicalException;

public class KafkaTechnicalException extends DokdistavstemmingTechnicalException {
	public KafkaTechnicalException(String s, Throwable t) {
		super(s, t);
	}
}
