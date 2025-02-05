package no.nav.dokdistavstemming.sdist006;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.springframework.stereotype.Component;

import static no.nav.dokdistavstemming.sdist006.DistribuerTilSentralPrintRoute.DIRECT_SENTRALPRINT;
import static no.nav.dokdistavstemming.sdist006.DistribuerTilSentralPrintRoute.PROPERTY_FORSENDELSE_ID;

@Slf4j
@Component
public class DistribuerTilSentralPrintMQService {
	private final CamelContext context;
	private final ProducerTemplate producerTemplate;

	public DistribuerTilSentralPrintMQService(CamelContext context, ProducerTemplate producerTemplate) {
		this.context = context;
		this.producerTemplate = producerTemplate;
	}

	public void sendToQdist009(long forsendelsesId) {

		Exchange exchange = new ExchangeBuilder(context)
				.withProperty(PROPERTY_FORSENDELSE_ID, forsendelsesId)
				.withBody(new DistribuerTilKanal(String.valueOf(forsendelsesId)))
				.build();
		producerTemplate.send(DIRECT_SENTRALPRINT, exchange);
	}

}
