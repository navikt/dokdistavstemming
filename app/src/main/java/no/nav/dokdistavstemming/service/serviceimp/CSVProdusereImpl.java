package no.nav.dokdistavstemming.service.serviceimp;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.domain.to.DokDistAvstemmingUtenPrintTo;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;
import no.nav.dokdistavstemming.metrics.Monitor;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
@Slf4j
public class CSVProdusereImpl implements CSVProdusere {


	private final static String CSV_FILTER_FIL = "dokdistcvs";

	@Monitor(value = "dokdist_request", extraTags = {"process_code", "oppretteCsvFil"}, percentiles = {0.5, 0.95})
	public File oppretteCsvFil(List<DokDistAvstemmingUtenPrintTo> dokDistAvstemmingForsendelser) throws IOException {

		if(dokDistAvstemmingForsendelser.isEmpty() || dokDistAvstemmingForsendelser.equals(null)){
			throw new DokDistAvstemmingFunctionalException("Fant ikke dokdistavstemming til å lage fil");
		}

		HashSet<String> kolonneNavn = new HashSet<>();
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema csvSchema = csvMapper.schemaFor(DokDistAvstemmingUtenPrintTo.class).withHeader().withLineSeparator("\r\n");

		for (CsvSchema.Column kolonne : csvSchema) {
			kolonneNavn.add(kolonne.getName());
		}

		SimpleBeanPropertyFilter csvResponseFiler = new SimpleBeanPropertyFilter.FilterExceptFilter(kolonneNavn);
		FilterProvider filterProvider = new SimpleFilterProvider().addFilter(CSV_FILTER_FIL, csvResponseFiler);

		File produced = File.createTempFile("dokdistavstemming-", ".csv", null);
		FileOutputStream fos = new FileOutputStream(produced);
		log.info(String.format(" mottal kall til å convertere list til fil med filnavn=%s",produced.getName()));
		csvMapper.setFilterProvider(filterProvider);
		csvMapper.setAnnotationIntrospector(new CsvAnnotationIntrospector());
		ObjectWriter objectWriter = csvMapper.writer(csvSchema);
		objectWriter.writeValue(fos, dokDistAvstemmingForsendelser);

		return produced;

	}


	private static class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {
		@Override
		public Object findFilterId(Annotated a) {
			return CSV_FILTER_FIL;
		}
	}
}
