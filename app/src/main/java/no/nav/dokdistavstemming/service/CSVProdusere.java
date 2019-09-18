package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.DokDistAvstemmingForsendelse;

import java.io.IOException;
import java.util.List;

public interface CSVProdusere {

	void oppretteCsvObject(List<DokDistAvstemmingForsendelse> dokDistAvstemmingForsendelser) throws IOException;
}
