package no.nav.dokdistavstemming.health;

import com.ibm.msg.client.jakarta.jms.DetailedIllegalStateException;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

@Component
public class JmsAvailabilityHealthIndicator implements HealthIndicator {

	private final ConnectionFactory connectionFactory;
	private final ApplicationAvailability applicationAvailability;
	private final ApplicationEventPublisher applicationEventPublisher;

	public JmsAvailabilityHealthIndicator(ConnectionFactory connectionFactory,
										  ApplicationAvailability applicationAvailability,
										  ApplicationEventPublisher applicationEventPublisher) {
		this.connectionFactory = connectionFactory;
		this.applicationEventPublisher = applicationEventPublisher;
		this.applicationAvailability = applicationAvailability;
	}

	@Override
	public Health health() {
		try {
			doHealthCheck(Health.up());
			if (applicationAvailability.getReadinessState() == REFUSING_TRAFFIC) {
				AvailabilityChangeEvent.publish(applicationEventPublisher, "JMS Connection OK", ACCEPTING_TRAFFIC);
			}
			return Health.up().build();
		} catch (DetailedIllegalStateException e) {
			if (applicationAvailability.getReadinessState() == ACCEPTING_TRAFFIC) {
				AvailabilityChangeEvent.publish(applicationEventPublisher, e, REFUSING_TRAFFIC);
			}
			return Health.down(e).build();
		} catch (Exception e) {
			return Health.down(e).build();
		}
	}

	private void doHealthCheck(Health.Builder builder) throws Exception {
		try (Connection connection = connectionFactory.createConnection()) {
			connection.start();
		}
	}
}
