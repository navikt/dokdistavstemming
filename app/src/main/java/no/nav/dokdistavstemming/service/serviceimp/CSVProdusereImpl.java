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
import no.nav.dokdistavstemming.domain.AvstemForsendelseResponseTo;
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
@Slf4j
public class CSVProdusereImpl implements CSVProdusere {


	private static final String CSV_FILTER_FIL = "dokdistcvs";

	public File oppretteCsvFil(List<AvstemForsendelseResponseTo> avstemForsendelseResponseTo) {
		File produced = null;

		HashSet<String> kolonneNavn = new HashSet<>();
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema csvSchema = csvMapper.schemaFor(AvstemForsendelseResponseTo.class).withHeader().withColumnSeparator(';');

		for (CsvSchema.Column kolonne : csvSchema) {
			kolonneNavn.add(kolonne.getName());
		}

		SimpleBeanPropertyFilter csvResponseFiler = new SimpleBeanPropertyFilter.FilterExceptFilter(kolonneNavn);
		FilterProvider filterProvider = new SimpleFilterProvider().addFilter(CSV_FILTER_FIL, csvResponseFiler);

		try {
			produced = File.createTempFile( "dokdistavstemming-" + avstemForsendelseResponseTo.get(0).getDistribusjonKanal()+"-", ".csv", null);
			FileOutputStream fos = new FileOutputStream(produced);
			log.info(String.format("Det mottatt kall til å convertere list til CSV-fil med filnavn=%s", produced.getName()));
			csvMapper.setFilterProvider(filterProvider);
			csvMapper.setAnnotationIntrospector(new CsvAnnotationIntrospector());
			ObjectWriter objectWriter = csvMapper.writer(csvSchema);
			objectWriter.writeValue(fos, avstemForsendelseResponseTo);

		} catch (IOException e) {
			try {
				throw new IOException(String.format("Ugyldig input. Kan ikke opprette csv fil med feilmelding=%s", e.getMessage()));
			} catch (IOException ex) {
				log.warn(String.format("feilmelding=%s", ex.getMessage()));
			}
		}
		return produced;
	}


	private static class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {
		@Override
		public Object findFilterId(Annotated a) {
			return CSV_FILTER_FIL;
		}
	}
}
