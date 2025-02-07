package no.nav.dokdistavstemming.health;

import com.ibm.msg.client.jakarta.jms.DetailedIllegalStateException;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.jms.JmsHealthIndicator;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

@Component
public class JmsAvailabilityHealthIndicator extends JmsHealthIndicator {

	private final ApplicationAvailability applicationAvailability;
	private final ApplicationEventPublisher applicationEventPublisher;

	public JmsAvailabilityHealthIndicator(ConnectionFactory connectionFactory,
										  ApplicationAvailability applicationAvailability,
										  ApplicationEventPublisher applicationEventPublisher) {
		super(connectionFactory);
		this.applicationEventPublisher = applicationEventPublisher;
		this.applicationAvailability = applicationAvailability;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		try {
			super.doHealthCheck(builder);
			if (applicationAvailability.getReadinessState() == REFUSING_TRAFFIC) {
				AvailabilityChangeEvent.publish(applicationEventPublisher, "JMS Connection OK", ACCEPTING_TRAFFIC);
			}
		} catch (DetailedIllegalStateException e) {
			if(applicationAvailability.getReadinessState() == ACCEPTING_TRAFFIC) {
				AvailabilityChangeEvent.publish(applicationEventPublisher, e, REFUSING_TRAFFIC);
			}
		}
	}
}
