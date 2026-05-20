package no.nav.dokdistavstemming.sdist002;

import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;
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
	private static final CsvMapper csvMapper;
	private static final CsvSchema csvSchema;
	private static final ObjectWriter objectWriter;

	static {
		CsvSchema tempSchema = new CsvMapper().schemaFor(UekspedertForsendelseDokument.class)
				.withHeader()
				.withColumnSeparator(';').sortedBy("forsendelseId");

		for (CsvSchema.Column kolonne : tempSchema) {
			kolonneNavn.add(kolonne.getName());
		}

		FilterProvider filterProvider = new SimpleFilterProvider()
				.addFilter(CSV_FILTER_FIL, new SimpleBeanPropertyFilter.FilterExceptFilter(kolonneNavn));

		csvMapper = CsvMapper.builder()
				.annotationIntrospector(new CsvAnnotationIntrospector())
				.filterProvider(filterProvider)
				.build();

		csvSchema = csvMapper.schemaFor(UekspedertForsendelseDokument.class)
				.withHeader()
				.withColumnSeparator(';').sortedBy("forsendelseId");

		objectWriter = csvMapper.writer(csvSchema);
	}

	public byte[] oppretteCsv(List<UekspedertForsendelseDokument> uekspedertForsendelseDokument, DistribusjonKanalCode distribusjonskanal) {
		try (ByteArrayOutputStream fos = new ByteArrayOutputStream()) {
			log.info("Konverterer dokumentliste til CSV-fil for distribusjonskanal={}", distribusjonskanal);
			objectWriter.writeValue(fos, uekspedertForsendelseDokument);
			return fos.toByteArray();
		} catch (IOException e) {
			log.error("Kan ikke opprette CSV-fil. message={}", e.getMessage(), e);
			return null;
		}
	}

	private static class CsvAnnotationIntrospector extends JacksonAnnotationIntrospector {
		@Override
		public Object findFilterId(MapperConfig<?> config, Annotated a) {
			return CSV_FILTER_FIL;
		}
	}
}
