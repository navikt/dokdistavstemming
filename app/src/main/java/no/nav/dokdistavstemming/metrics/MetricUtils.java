package no.nav.dokdistavstemming.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokdistavstemming.exceptions.DokDistAvstemmingFunctionalException;
import org.springframework.stereotype.Component;

@Component
public class MetricUtils {

	public static final String DOK_REQUEST_FUNCTIONAL_EXCEPTION_COUNTER = "dok_request_functional_exception_counter";
	public static final String DOK_REQUEST_FUNCTIONAL_COUNTER = "dok_request_functional_counter";
	public static final String TAG_BESKRIVELSE = "beskrivelse";

	private final MeterRegistry meterRegistry;

	public MetricUtils(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public Counter initFunctionalCounter(String beskrivelse, String aksjon, String hjemmel){
		return Counter.builder(DOK_REQUEST_FUNCTIONAL_COUNTER)
				.tag(TAG_BESKRIVELSE, beskrivelse)
				.register(meterRegistry);
	}

	public Counter initExceptionCounter(String errorType, String exceptionName){
		return Counter.builder(DOK_REQUEST_FUNCTIONAL_EXCEPTION_COUNTER)
				.tag("error_type", errorType)
				.tag("exception_name", exceptionName)
				.register(meterRegistry);
	}



	public static boolean isFunctionalException(Throwable e) {
		return e instanceof DokDistAvstemmingFunctionalException;
	}
}
