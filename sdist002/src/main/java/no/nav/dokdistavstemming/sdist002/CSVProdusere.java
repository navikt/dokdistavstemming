package no.nav.dokdistavstemming.sdist002;

import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;

import java.io.File;
import java.util.List;

public interface CSVProdusere {
	File oppretteCsvFil(List<UekspedertForsendelseDokument> uekspedertForsendelseDokument);
}
