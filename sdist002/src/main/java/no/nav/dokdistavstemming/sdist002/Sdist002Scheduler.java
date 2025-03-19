package no.nav.dokdistavstemming.sdist002;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import no.nav.dokdistavstemming.service.SlackService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Sdist002Scheduler {

	private final Sdist002Service sdist002Service;
	private final LeaderElectionConsumer leaderElectionConsumer;
	private final SlackService slackService;

	public Sdist002Scheduler(Sdist002Service sdist002Service,
							 LeaderElectionConsumer leaderElectionConsumer,
							 SlackService slackService) {
		this.sdist002Service = sdist002Service;
		this.leaderElectionConsumer = leaderElectionConsumer;
		this.slackService = slackService;
	}

	@Scheduled(cron = "${sdist002.cron.job}")
	public void configureTasks() {
		if (leaderElectionConsumer.isLeader()) {
			log.info("Sdist002 cron-jobb starter");

			try {
				sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal();
			} catch (Exception e) {
				var feilmelding = "Sdist002 cron-jobb feilet med feilmelding=%s".formatted(e.getMessage());

				log.error(feilmelding, e);
				slackService.sendMelding(feilmelding);
			} finally {
				log.info("Sdist002 er ferdig");
			}
		}
	}

}
