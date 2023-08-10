package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
public class Sdist006Scheduler {

	private final LeaderElectionConsumer leaderElection;
	private final ThreadPoolTaskExecutor poolTaskExecutor;
	private final SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService;

	public Sdist006Scheduler(ThreadPoolTaskExecutor poolTaskExecutor, LeaderElectionConsumer leaderElection, SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService) {
		this.leaderElection = leaderElection;
		this.poolTaskExecutor = poolTaskExecutor;
		this.sendUlesteForsendelserTilSentralPrintService = sendUlesteForsendelserTilSentralPrintService;
	}

	//Venter med å skru på scheduling så den kan safely merges inn i master før den er ferdig
	//@Scheduled(cron = "${sdist006.cron.job}")
	public void runSdist006() {
		//Finnes det en MDC-verdi som kan addes her for å wrappe hele sdist006-kjøringen?
		//x_batchId elns. Så kan man tracke individuell change med correlationId og hele "batchen" med batchId
		if (leaderElection.isLeader()) {
			log.info("Starter sdist006 cron-jobb");
			poolTaskExecutor.execute(sendUlesteForsendelserTilSentralPrintService::sendUlesteForsendelserTilSentralPrint);
			log.info("Avslutter sdist006 cron-job");
		}
	}
}
