package no.nav.dokdistavstemming.sdist002;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Sdist002Scheduler {

	private final Sdist002Service sdist002Service;
	private final LeaderElectionConsumer leaderElectionConsumer;

	public Sdist002Scheduler(Sdist002Service sdist002Service,
							 LeaderElectionConsumer leaderElectionConsumer) {
		this.sdist002Service = sdist002Service;
		this.leaderElectionConsumer = leaderElectionConsumer;
	}

	@Scheduled(cron = "${sdist002.cron.job}")
	public void configureTasks() {
		if (leaderElectionConsumer.isLeader()) {
			log.info("Sdist002 cron-jobb starter");
			sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal();
			log.info("Sdist002 er ferdig");
		}
	}

}
