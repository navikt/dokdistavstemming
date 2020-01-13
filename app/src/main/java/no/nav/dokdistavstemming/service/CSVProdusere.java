package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

public interface CSVProdusere {

	File oppretteCsvFil(List<AvstemForsendelseResponseTo> avstemForsendelseResponseTo);
}
