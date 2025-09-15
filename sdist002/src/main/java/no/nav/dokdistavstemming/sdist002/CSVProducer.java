package no.nav.dokdistavstemming.sdist002;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import no.nav.dokdistavstemming.domain.enums.DistribusjonKanalCode;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;


@Component
@Slf4j
public class CSVProducer {

	private static final String CSV_FILTER_FIL = "dokdistcsv";
	private static final HashSet<String> kolonneNavn = new HashSet<>();
	private static final CsvMapper csvMapper = new CsvMapper();
	private static final CsvSchema csvSchema = csvMapper.schemaFor(UekspedertForsendelseDokument.class)
			.withHeader()
			.withColumnSeparator(';').sortedBy("forsendelseId");
	private static final ObjectWriter objectWriter;

	static {
		csvMapper.registerModule(new JavaTimeModule());
		objectWriter = csvMapper.writer(csvSchema);

		for (CsvSchema.Column kolonne : csvSchema) {
			kolonneNavn.add(kolonne.getName());
		}
	}

	private static final SimpleBeanPropertyFilter csvResponseFiler = new SimpleBeanPropertyFilter.FilterExceptFilter(kolonneNavn);
	private static final FilterProvider filterProvider = new SimpleFilterProvider().addFilter(CSV_FILTER_FIL, csvResponseFiler);

	public byte[] oppretteCsv(List<UekspedertForsendelseDokument> uekspedertForsendelseDokument, DistribusjonKanalCode distribusjonskanal) {
		try (ByteArrayOutputStream fos = new ByteArrayOutputStream()) {
			log.info("Konverterer dokumentliste til CSV-fil for distribusjonskanal={}", distribusjonskanal);
			csvMapper.setFilterProvider(filterProvider);
			csvMapper.setAnnotationIntrospector(new CsvAnnotationIntrospector());
			objectWriter.writeValue(fos, uekspedertForsendelseDokument);
			return fos.toByteArray();
		} catch (IOException e) {
			log.error("Kan ikke opprette CSV-fil. message={}", e.getMessage(), e);
			return null;
		}
	}

	private static class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {
		@Override
		public Object findFilterId(Annotated a) {
			return CSV_FILTER_FIL;
		}
	}
}
