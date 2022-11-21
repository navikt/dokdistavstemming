package no.nav.dokdistavstemming.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import no.nav.dokdistavstemming.sdist002.serviceimp.Sdist002Service;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Configuration
@EnableScheduling
@Slf4j
public class Sdist002ScheduleConfig {

	private final Sdist002Service sdist002Service;
	private final LeaderElectionConsumer leaderElection;

	public Sdist002ScheduleConfig(Sdist002Service sdist002Service, LeaderElectionConsumer leaderElection) {
		this.sdist002Service = sdist002Service;
		this.leaderElection = leaderElection;
	}

	@Scheduled(cron = "${sdist002.cron.job}")
	public void configureTasks() {
		if (leaderElection.isLeader()) {
			log.info("Starter sdist002 cron-jobb ...");
			sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal();
		}
	}

}