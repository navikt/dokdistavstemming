package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;

import java.io.File;
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

public interface CSVProdusere {

	File rulesToCsv(List<DokDistAvStemmingResponseTo> dokDistAvStemmingResponseTo) throws Exception;

}
