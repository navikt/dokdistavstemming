package no.nav.dokdistavstemming.service;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import no.nav.dokdistavstemming.domain.HentUekspederForsendelseResponseTo;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Component
public class CSVProdusereImpl implements CSVProdusere {

	private final static String CSV_FILTER_FIL = "dokdistcvs";


	@Override
	public void oppretteCsvObject(List<HentUekspederForsendelseResponseTo> hentUekspederForsendelserResponseTo) throws IOException {
		HashSet<String> kolonneNavn = new HashSet<>();
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema csvSchema = csvMapper.schemaFor(HentUekspederForsendelseResponseTo.class).withHeader().withLineSeparator("\r\n");

		for (CsvSchema.Column kolonne : csvSchema) {
			kolonneNavn.add(kolonne.getName());
		}

		SimpleBeanPropertyFilter csvResponseFiler = new SimpleBeanPropertyFilter.FilterExceptFilter(kolonneNavn);
		FilterProvider filterProvider = new SimpleFilterProvider().addFilter(CSV_FILTER_FIL, csvResponseFiler);


		csvMapper.setFilterProvider(filterProvider);
		csvMapper.setAnnotationIntrospector(new CsvAnnotationIntrospector());
		ObjectWriter objectWriter = csvMapper.writer(csvSchema);

		File produced = File.createTempFile("produced", ".csv", null);
		FileOutputStream fos = new FileOutputStream(produced);
		objectWriter.writeValue(fos, hentUekspederForsendelserResponseTo);


	}


	private static class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {
		@Override
		public Object findFilterId(Annotated a) {
			return CSV_FILTER_FIL;
		}
	}
}
