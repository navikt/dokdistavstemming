package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;

import java.io.IOException;
import java.util.List;

public interface CSVProdusere {

	void oppretteCsvObject(List<HentUekspederForsendelseResponseTo> hentUekspederForsendelserResponseTo) throws IOException;
}
