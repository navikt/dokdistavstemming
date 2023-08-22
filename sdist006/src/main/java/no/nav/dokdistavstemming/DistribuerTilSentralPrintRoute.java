package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import javax.jms.Queue;
import javax.xml.bind.JAXBException;

import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.WARN;

@Slf4j
@Component
public class DistribuerTilSentralPrintRoute extends RouteBuilder {

	public static String DIRECT_SENTRALPRINT = "direct:sentralprint";
	private final Queue qdist009;

	public DistribuerTilSentralPrintRoute(Queue qdist009) {
		this.qdist009 = qdist009;
	}


	@Override
	public void configure() throws Exception {
		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.logStackTrace(true)
				.loggingLevel(ERROR));

		onException(JAXBException.class)
				.handled(true)
				.useOriginalMessage()
				.log(WARN, log, "${exception};");

		from(DIRECT_SENTRALPRINT)
				.process(exchange ->
						log.info(exchange.getIn().getBody(String.class)))
				.log("Mottatt bestilling med bestillingsId: ")
				.routeId("sentralprint_route")
				.to("jms:" + qdist009.getQueueName());
	}

}
