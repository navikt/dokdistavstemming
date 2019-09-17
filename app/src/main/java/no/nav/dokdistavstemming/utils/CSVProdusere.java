package no.nav.dokdistavstemming.utils;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import no.nav.dokdistavstemming.domain.DokDistAvstemmingForsendelse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;

public class CSVProdusere {

	private final static String CSV_FILTER_FIL = "dokdistcvs";

	public void oppretteCsvObject(OutputStream outputStream,
								  List<DokDistAvstemmingForsendelse> dokDistAvstemmingForsendelser, CsvSchema csvSchema) throws IOException {
		HashSet<String> kolonneNavn = new HashSet<>();

		for (CsvSchema.Column kolonne : csvSchema) {
			kolonneNavn.add(kolonne.getName());
		}

		SimpleBeanPropertyFilter csvResponseFiler = new SimpleBeanPropertyFilter.FilterExceptFilter(kolonneNavn);
		FilterProvider filterProvider = new SimpleFilterProvider().addFilter(CSV_FILTER_FIL, csvResponseFiler);

		CsvMapper csvMapper = new CsvMapper();
		csvMapper.setFilterProvider(filterProvider);
		csvMapper.setAnnotationIntrospector(new CsvAnnotationIntrospector());
		ObjectWriter objectWriter = csvMapper.writer(csvSchema);
		objectWriter.writeValue(outputStream, dokDistAvstemmingForsendelser);


	}

	private static class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {
		@Override
		public Object findFilterId(Annotated a) {
			return CSV_FILTER_FIL;
		}
	}
}
