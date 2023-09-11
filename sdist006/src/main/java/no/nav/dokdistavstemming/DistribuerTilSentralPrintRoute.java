package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import javax.jms.Queue;
import javax.xml.bind.JAXBException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.bind.JAXBContext.newInstance;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@Slf4j
@Component
public class DistribuerTilSentralPrintRoute extends RouteBuilder {

	public static String DIRECT_SENTRALPRINT = "direct:sentralprint";
	public static final String PROPERTY_FORSENDELSE_ID = "forsendelseId";
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
				.log(WARN, log, "sdist006 feilet med å legge forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "} på kø til qdist009")
				.log(WARN, log, "${exception};");

		from(DIRECT_SENTRALPRINT)
				.routeId("sentralprint_route")
				.setProperty(PROPERTY_FORSENDELSE_ID, simple("${body.forsendelseId}"))
				.marshal(new JaxbDataFormat(newInstance(DistribuerTilKanal.class)))
				.convertBodyTo(String.class, UTF_8.toString())
				.to("jms:" + qdist009.getQueueName())
				.log(INFO, log, "sdist006 har lagt forsendelse med forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "} på kø til qdist009 for distribusjon av forsendelse til print");
	}

}
