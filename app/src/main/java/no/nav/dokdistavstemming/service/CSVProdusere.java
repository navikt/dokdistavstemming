package no.nav.dokdistavstemming.service;

import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CSVProdusere {

	File oppretteCsvObject(List<DokDistAvStemmingResponseTo> dokDistAvStemmingResponseTo);

}
