package no.nav.dokdistavstemming.sdist004;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import no.nav.dokdistavstemming.service.SlackService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Sdist004Scheduler {

	private final BulkOppdaterJournalpostDistInfoService bulkOppdaterJournalpostDistInfoService;
	private final LeaderElectionConsumer leaderElectionConsumer;
	private final SlackService slackService;

	public Sdist004Scheduler(BulkOppdaterJournalpostDistInfoService bulkOppdaterJournalpostDistInfoService,
							 LeaderElectionConsumer leaderElectionConsumer,
							 SlackService slackService) {
		this.bulkOppdaterJournalpostDistInfoService = bulkOppdaterJournalpostDistInfoService;
		this.leaderElectionConsumer = leaderElectionConsumer;
		this.slackService = slackService;
	}

	@Scheduled(cron = "${sdist004.cron.job}")
	public void configureTasks() {
		if (leaderElectionConsumer.isLeader()) {
			log.info("Sdist004 cron-jobb starter");

			try {
				bulkOppdaterJournalpostDistInfoService.oppdaterAvstemOgJournalpostDistInfo();
			} catch (Exception e) {
				var feilmelding = "Sdist004 cron-jobb feilet med feilmelding=%s".formatted(e.getMessage());

				log.error(feilmelding, e);
				slackService.sendMelding(feilmelding);
			} finally {
				log.info("Sdist004 er ferdig");
			}
		}
	}
}
