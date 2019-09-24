package no.nav.dokdistavstemming.service.serviceimp;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.reactivex.internal.schedulers.IoScheduler;
import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;
import no.nav.dokdistavstemming.service.CSVProdusere;
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

	private final static String CSV_FILTER_NAME = "csvFilter";

	@Override
	public File oppretteCsvObject(List<DokDistAvStemmingResponseTo> dokDistAvStemmingResponseTo){

		HashSet<String> columnNames = new HashSet<String>();
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema csvSchema = csvMapper.schemaFor(DokDistAvStemmingResponseTo.class).withHeader().withLineSeparator("\r\n");

		for (CsvSchema.Column column : csvSchema) {
			columnNames.add(column.getName());
		}

		SimpleBeanPropertyFilter csvReponseFilter =
				new SimpleBeanPropertyFilter.FilterExceptFilter(columnNames);
		FilterProvider filterProvider = new SimpleFilterProvider().addFilter(CSV_FILTER_NAME, csvReponseFilter);

		csvMapper.setFilterProvider(filterProvider);
		csvMapper.setAnnotationIntrospector(new CsvAnnotationIntrospector());

		try{
			ObjectWriter objectWriter = csvMapper.writer(csvSchema);
			File produced = File.createTempFile("produced", ".csv");
			FileOutputStream fos = new FileOutputStream(produced);
			objectWriter.writeValue(produced, dokDistAvStemmingResponseTo);
			return produced;

		} catch (IOException e){
			throw  new DokDistAvstemmingFunctionalException("");
		}



	}


	private class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {
		@Override
		public Object findFilterId(Annotated a) {
			return CSV_FILTER_NAME;
		}
	}
}
