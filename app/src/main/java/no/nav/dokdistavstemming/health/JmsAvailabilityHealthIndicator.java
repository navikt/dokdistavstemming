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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

@Component
public class JmsAvailabilityHealthIndicator implements HealthIndicator {
	private static final long JMS_HEALTH_CHECK_TIMEOUT_SECONDS = 10;

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
			doHealthCheck();
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

	private void doHealthCheck() throws Exception {
		try (var executor = Executors.newSingleThreadExecutor()) {
			var check = executor.submit(() -> {
				try (Connection connection = connectionFactory.createConnection()) {
					connection.start();
				}
				return null;
			});
			try {
				check.get(JMS_HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (ExecutionException e) {
				if (e.getCause() instanceof Exception exception) {
					throw exception;
				}
				throw new IllegalStateException("Uventet feil ved health check av JMS connection", e.getCause());
			} catch (TimeoutException e) {
				check.cancel(true);
				throw new IllegalStateException("Timeout ved health check av JMS connection", e);
			}
		}
	}
}
