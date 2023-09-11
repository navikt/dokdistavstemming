package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.springframework.stereotype.Component;

import static no.nav.dokdistavstemming.DistribuerTilSentralPrintRoute.DIRECT_SENTRALPRINT;
import static no.nav.dokdistavstemming.DistribuerTilSentralPrintRoute.PROPERTY_FORSENDELSE_ID;

@Slf4j
@Component
public class DistribuerTilSentralPrintMQService {
	private final CamelContext context;
	private final ProducerTemplate producerTemplate;

	public DistribuerTilSentralPrintMQService(CamelContext context, ProducerTemplate producerTemplate) {
		this.context = context;
		this.producerTemplate = producerTemplate;
	}

	//bytter tilbake til long etter prod-verifisering av mq
	public void sendToQdist009(String forsendelsesId) {

		Exchange exchange = new ExchangeBuilder(context)
				.withProperty(PROPERTY_FORSENDELSE_ID, forsendelsesId)
				.withBody(new DistribuerTilKanal(String.valueOf(forsendelsesId)))
				.build();
		producerTemplate.send(DIRECT_SENTRALPRINT, exchange);
	}

}
