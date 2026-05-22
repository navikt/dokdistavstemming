package no.nav.dokdistavstemming;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import no.nav.dokdistavstemming.sdist002.Sdist002Scheduler;
import no.nav.dokdistavstemming.sdist004.Sdist004Scheduler;
import no.nav.dokdistavstemming.sdist006.Sdist006Scheduler;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.junit.jupiter.api.Test;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = {Application.class, ApplicationIT.Config.class}, webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("itest")
public class ApplicationIT {

	@Configuration
	public static class Config {
		@Bean
		public Queue qdist009(@Value("${dokdistsentralprint_qdist009_dist_s_print.queuename}") String qdist009QueueName) {
			return new ActiveMQQueue(qdist009QueueName);
		}

		@Bean(initMethod = "start", destroyMethod = "stop")
		public EmbeddedActiveMQ activeMQServer() {
			EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();
			embeddedActiveMQ.setConfigResourcePath("artemis-server.xml");
			return embeddedActiveMQ;
		}

		@Bean
		@DependsOn("activeMQServer")
		public ConnectionFactory connectionFactory() {
			ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://0");
			JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
			pooledFactory.setConnectionFactory(activeMQConnectionFactory);
			pooledFactory.setMaxConnections(1);
			return pooledFactory;
		}
	}

	@Autowired
	private RestTestClient restTestClient;

	@Autowired
	private Sdist002Scheduler sdist002Scheduler;

	@Autowired
	private Sdist004Scheduler sdist004Scheduler;

	@Autowired
	private Sdist006Scheduler sdist006Scheduler;

	@Test
	void shouldStartApp() {
		// verifisere at alle schedulers er inkludert i app bygget
		assertThat(sdist002Scheduler).isNotNull();
		assertThat(sdist004Scheduler).isNotNull();
		assertThat(sdist006Scheduler).isNotNull();

		doHealthCheck("/actuator/health/liveness");
		doHealthCheck("/actuator/health/readiness");
	}

	private void doHealthCheck(String uri) {
		restTestClient.get()
			.uri(uri)
			.exchange()
			.expectStatus().isOk()
			.expectBody().json("{\"status\":\"UP\"}");
	}
}
