package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.Exceptions.KafkaTechnicalException;
import no.nav.doknotifikasjon.schemas.DoknotifikasjonStopp;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.ExecutionException;

import static no.nav.dokdistavstemming.utils.Sdist006utils.getCallId;

@Slf4j
@Component
@EnableTransactionManagement
class KafkaEventProducer {

	private static final String KAFKA_NOT_AUTHENTICATED = "Not authenticated to publish to topic: ";
	private static final String KAFKA_FAILED_TO_SEND = "Failed to send message to kafka. Topic: ";
	private static final String RENOTIFIKASJON_STOPP_TOPIC = "teamdokumenthandtering.privat-dok-notifikasjon-stopp";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Retryable(backoff = @Backoff(delay = 500))
	void publish(DoknotifikasjonStopp event) {

		ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
				RENOTIFIKASJON_STOPP_TOPIC,
				null,
				System.currentTimeMillis(),
				getCallId(),
				event
		);

		try {
			SendResult<String, Object> sendResult = kafkaTemplate.send(producerRecord).get();
			log.info("Sdist006 har skrevet avbryt renotifikasjon event til topic={} for bestillingsId={}. hendelseMetadata={}",
					RENOTIFIKASJON_STOPP_TOPIC, event.getBestillingsId(), sendResult.getRecordMetadata()
			);
		} catch (ExecutionException executionException) {
			if (executionException.getCause() instanceof KafkaProducerException kafkaProducerException) {
				if (kafkaProducerException.getCause() instanceof TopicAuthorizationException) {
					throw new KafkaTechnicalException(KAFKA_NOT_AUTHENTICATED + RENOTIFIKASJON_STOPP_TOPIC, kafkaProducerException.getCause());
				}
			}
			throw new KafkaTechnicalException(KAFKA_FAILED_TO_SEND + RENOTIFIKASJON_STOPP_TOPIC, executionException);
		} catch (Exception e) {
			throw new KafkaTechnicalException(KAFKA_FAILED_TO_SEND + RENOTIFIKASJON_STOPP_TOPIC, e);
		}
	}
}
