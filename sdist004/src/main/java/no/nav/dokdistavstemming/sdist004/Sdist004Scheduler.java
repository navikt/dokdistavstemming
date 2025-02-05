package no.nav.dokdistavstemming.sdist004;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Sdist004Scheduler {

	private final BulkOppdaterJournalpostDistInfoService bulkOppdaterJournalpostDistInfoService;
	private final LeaderElectionConsumer leaderElectionConsumer;

	public Sdist004Scheduler(BulkOppdaterJournalpostDistInfoService bulkOppdaterJournalpostDistInfoService,
							 LeaderElectionConsumer leaderElectionConsumer) {
		this.bulkOppdaterJournalpostDistInfoService = bulkOppdaterJournalpostDistInfoService;
		this.leaderElectionConsumer = leaderElectionConsumer;
	}

	@Scheduled(cron = "${sdist004.cron.job}")
	public void configureTasks() {
		if (leaderElectionConsumer.isLeader()) {
			log.info("Sdist004 cron-jobb starter");
			bulkOppdaterJournalpostDistInfoService.oppdaterAvstemOgJournalpostDistInfo();
			log.info("Sdist004 er ferdig");
		}
	}
}
