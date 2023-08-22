package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.springframework.stereotype.Component;

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
				.withBody(forsendelsesId)//Qdist009Forsendelse.builder().forsendelseId(forsendelsesId))
				.build();
		log.info("Sender print-bestilling med forsendelsesId={} til qdist009", forsendelsesId);
		producerTemplate.send(DistribuerTilSentralPrintRoute.DIRECT_SENTRALPRINT, exchange);
	}

}
