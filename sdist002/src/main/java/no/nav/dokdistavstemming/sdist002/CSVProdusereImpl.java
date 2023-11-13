package no.nav.dokdistavstemming.sdist002;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.domain.UekspedertForsendelseDokument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;


@Component
@Slf4j
public class CSVProdusereImpl implements CSVProdusere {

	private static final String CSV_FILTER_FIL = "dokdistcsv";
	private static final String BASE_TMP_DIRECTORY = System.getProperty("java.io.tmpdir");

	public File oppretteCsvFil(List<UekspedertForsendelseDokument> uekspedertForsendelseDokument) {
		HashSet<String> kolonneNavn = new HashSet<>();
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema csvSchema = csvMapper.schemaFor(UekspedertForsendelseDokument.class)
				.withHeader()
				.withColumnSeparator(';').sortedBy("forsendelseId");

		for (CsvSchema.Column kolonne : csvSchema) {
			kolonneNavn.add(kolonne.getName());
		}

		SimpleBeanPropertyFilter csvResponseFiler = new SimpleBeanPropertyFilter.FilterExceptFilter(kolonneNavn);
		FilterProvider filterProvider = new SimpleFilterProvider().addFilter(CSV_FILTER_FIL, csvResponseFiler);
		String localDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		String distribusjonKanal = uekspedertForsendelseDokument.get(0).getDistribusjonKanal();

		File produced = new File(BASE_TMP_DIRECTORY + "/dokdistavstemming-" + distribusjonKanal + "-" + localDate + ".csv");
		try (FileOutputStream fos = new FileOutputStream(produced)) {
			log.info("Konverterer dokumentliste til CSV-fil med filnavn={}", produced.getName());
			csvMapper.setFilterProvider(filterProvider);
			csvMapper.setAnnotationIntrospector(new CsvAnnotationIntrospector());
			ObjectWriter objectWriter = csvMapper.writer(csvSchema);
			objectWriter.writeValue(fos, uekspedertForsendelseDokument);
			return produced;
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
