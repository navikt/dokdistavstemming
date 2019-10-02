package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.to.DokDistAvstemmingUtenPrintTo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

public interface CSVProdusere {

	File oppretteCsvFil(List<DokDistAvstemmingUtenPrintTo> dokDistAvstemmingForsendelser) throws IOException;
}
