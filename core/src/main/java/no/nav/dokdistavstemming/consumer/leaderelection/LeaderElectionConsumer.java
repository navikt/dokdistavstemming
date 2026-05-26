package no.nav.dokdistavstemming.consumer.leaderelection;

import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.net.InetAddress.getLocalHost;

@Slf4j
@Component
public class LeaderElectionConsumer {
	private final WebClient webClient;
	private final JsonMapper mapper;

	public LeaderElectionConsumer(WebClient.Builder webClientBuilder,
								  JsonMapper mapper,
								  @Value("${elector.path}") String electorPath) {
		this.webClient = webClientBuilder
				.baseUrl(electorPath.startsWith("http") ? electorPath : "http://" + electorPath)
				.build();
		this.mapper = mapper;
	}

	/**
	 * @return true hvis denne podden er leader, ellers false
	 */
	public boolean isLeader() {
		return Boolean.TRUE.equals(isLeaderAsync().block());
	}

	public Mono<Boolean> isLeaderAsync() {
		return webClient.get()
				.retrieve()
				.bodyToMono(String.class)
				.map(response -> {
					try {
						String leader = mapper.readTree(response).get("name").asText();
						String hostname = getLocalHost().getHostName();
						return hostname.equals(leader);
					} catch (Exception e) {
						log.error("Kunne ikke bestemme lederpod. Feilmelding: {}", e.getMessage());
						return false;
					}
				});
	}
}
