package no.nav.dokdistavstemming.sdist006;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;

@Slf4j
@Component
public class Sdist006Scheduler {

	private final ApplicationAvailability applicationAvailability;
	private final LeaderElectionConsumer leaderElectionConsumer;
	private final SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService;

	public Sdist006Scheduler(ApplicationAvailability applicationAvailability,
							 LeaderElectionConsumer leaderElectionConsumer,
							 SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService) {
		this.applicationAvailability = applicationAvailability;
		this.leaderElectionConsumer = leaderElectionConsumer;
		this.sendUlesteForsendelserTilSentralPrintService = sendUlesteForsendelserTilSentralPrintService;
	}

	@Scheduled(cron = "${sdist006.cron.job}")
	public void runSdist006() {
		if (leaderElectionConsumer.isLeader()) {
			if (applicationAvailability.getReadinessState() == ACCEPTING_TRAFFIC) {
				log.info("Sdist006 cron-jobb starter");
				sendUlesteForsendelserTilSentralPrintService.sendUlesteForsendelserTilSentralPrint();
				log.info("Sdist006 er ferdig");
			} else {
				handleRefusingTraffic();
			}
		}
	}

	private void handleRefusingTraffic() {
		Object source = applicationAvailability.getLastChangeEvent(ReadinessState.class).getSource();
		if (source instanceof AnnotationConfigServletWebServerApplicationContext) {
			log.info("Sdist006 kan ikke starte. Graceful shutdown");
		} else if (source instanceof Throwable throwable) {
			log.error("Sdist006 kan ikke starte pga feil. melding={}", throwable, throwable);
		} else {
			log.error("Sdist006 kan ikke starte av ukjent årsak. melding={}", source);
		}
	}
}
