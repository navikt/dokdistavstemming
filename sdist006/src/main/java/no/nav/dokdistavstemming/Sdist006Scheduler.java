package no.nav.dokdistavstemming;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.consumer.leaderelection.LeaderElectionConsumer;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_BATCh_ID;

@Slf4j
@Configuration
public class Sdist006Scheduler {

	private final LeaderElectionConsumer leaderElection;
	private final ThreadPoolTaskExecutor poolTaskExecutor;
	private final SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "dd.MM.HH.mm" );

	public Sdist006Scheduler(ThreadPoolTaskExecutor poolTaskExecutor, LeaderElectionConsumer leaderElection, SendUlesteForsendelserTilSentralPrintService sendUlesteForsendelserTilSentralPrintService) {
		this.leaderElection = leaderElection;
		this.poolTaskExecutor = poolTaskExecutor;
		this.sendUlesteForsendelserTilSentralPrintService = sendUlesteForsendelserTilSentralPrintService;
	}

	@Scheduled(cron = "${sdist006.cron.job}")
	public void runSdist006() {
		if (leaderElection.isLeader()) {
			try {
				MDC.put(MDC_BATCh_ID, LocalDateTime.now().format(formatter));
				log.info("Starter sdist006 cron-jobb");
				poolTaskExecutor.execute(sendUlesteForsendelserTilSentralPrintService::sendUlesteForsendelserTilSentralPrint);
			} finally {
				MDC.clear();
			}
		}
	}
}
