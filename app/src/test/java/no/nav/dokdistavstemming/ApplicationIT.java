package no.nav.dokdistavstemming;

import com.ibm.mq.jakarta.jms.MQQueue;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import no.nav.dokdistavstemming.sdist002.Sdist002Scheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(classes = {Application.class, ApplicationIT.Config.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("itest")
public class ApplicationIT {

	public static class Config {
		@Bean
		public Queue qdist009Queue(@Value("${dokdistsentralprint_qdist009_dist_s_print.queuename}") String qdist009QueueName) throws JMSException {
			return new MQQueue(qdist009QueueName);
		}
	}

	private final TestRestTemplate testRestTemplate;
	private final Sdist002Scheduler sdist002Scheduler;
	private final Sdist004Scheduler sdist004Scheduler;
	private final Sdist006Scheduler sdist006Scheduler;

	@Autowired
	public ApplicationIT(TestRestTemplate testRestTemplate,
						 Sdist002Scheduler sdist002Scheduler,
						 Sdist004Scheduler sdist004Scheduler,
						 Sdist006Scheduler sdist006Scheduler) {
		this.testRestTemplate = testRestTemplate;
		this.sdist002Scheduler = sdist002Scheduler;
		this.sdist004Scheduler = sdist004Scheduler;
		this.sdist006Scheduler = sdist006Scheduler;
	}

	@Test
	void shouldStartApp() {
		// verifisere at alle schedulers er inkludert i app bygget
		assertThat(sdist002Scheduler).isNotNull();
		assertThat(sdist004Scheduler).isNotNull();
		assertThat(sdist006Scheduler).isNotNull();

		// verifisere at appen klarer starte opp
		var liveness = testRestTemplate.getForEntity("/actuator/health/liveness", String.class);
		assertThat(liveness.getStatusCode()).isEqualTo(OK);
		assertThat(liveness.getBody()).contains("UP");
	}
}
