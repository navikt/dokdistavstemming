package no.nav.dokdistavstemming.sdist002;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
public class Sdist002Scheduler {

	private final Sdist002Service sdist002Service;
	private final LeaderElectionConsumer leaderElection;
	private final ThreadPoolTaskExecutor poolTaskExecutor;

	public Sdist002Scheduler(ThreadPoolTaskExecutor poolTaskExecutor,
							 Sdist002Service sdist002Service, LeaderElectionConsumer leaderElection) {
		this.sdist002Service = sdist002Service;
		this.leaderElection = leaderElection;
		this.poolTaskExecutor = poolTaskExecutor;
	}

	@Scheduled(cron = "${sdist002.cron.job}")
	public void configureTasks() {
		if (leaderElection.isLeader()) {
			log.info("Starter sdist002 cron-jobb");
			poolTaskExecutor.execute(sdist002Service::oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal);
		}
	}

}
