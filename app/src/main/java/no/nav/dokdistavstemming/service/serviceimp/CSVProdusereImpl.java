package no.nav.dokdistavstemming.service.serviceimp;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.domain.DokDistAvStemmingResponseTo;
import no.nav.dokdistavstemming.service.CSVProdusere;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Service
@Slf4j
public class CSVProdusereImpl implements CSVProdusere {

	private final static String CSV_FILTER_NAME = "csvFilter";

	@Override
	public File rulesToCsv(List<DokDistAvStemmingResponseTo> dokDistAvStemmingResponseTo) throws IOException {
		CsvMapper csvMapper = new CsvMapper();
		CsvSchema csvSchema = csvMapper.schemaFor(DokDistAvStemmingResponseTo.class).withHeader().withColumnSeparator(';');
		csvMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		File createCsvFile = File.createTempFile("produced", ".csv");
		FileOutputStream outputStream = new FileOutputStream(createCsvFile);

		try (SequenceWriter csvWriter = csvMapper
				.addMixIn(DokDistAvStemmingResponseTo.class, DokDistAvStemmingResponseTo.class)
				.writerWithDefaultPrettyPrinter()
				.with(csvSchema)
				.with(new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
				.forType(DokDistAvStemmingResponseTo.class)
				.writeValues(outputStream)) {
			for (Object dokDistObject : dokDistAvStemmingResponseTo) {
				csvWriter.write(dokDistObject);
			}
		}

		return createCsvFile;
	}


}
