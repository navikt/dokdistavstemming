package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.DokDistAvstemmingResponseTo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

public interface CSVProdusere {

	File oppretteCsvFil(List<DokDistAvstemmingResponseTo> dokDistAvstemmingForsendelser) throws IOException;
}
