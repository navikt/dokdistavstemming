package no.nav.dokdistavstemming.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Slf4j
@EnableScheduling
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		// Sdist002, Sdist004, Sdist006
		threadPoolTaskScheduler.setPoolSize(3);
		threadPoolTaskScheduler.setVirtualThreads(true);
		threadPoolTaskScheduler.initialize();
		taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
	}
}
