package no.nav.dokdistavstemming.scheduler;

import org.springframework.beans.factory.annotation.Value;

public class SchedulerProperties {

	@Value("${scheduler_interval_minutes:1440}")
	private Integer syncInterval;

	@Value("${hostname}")
	private String hostname;

	public Integer getSyncInterval() {
		return syncInterval;
	}

	public String getHostname() {
		return hostname;
	}

}
