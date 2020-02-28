package no.nav.dokdistavstemming.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.domain.DistribusjonKanalCode;
import no.nav.dokdistavstemming.service.serviceimp.Sdist002Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Arrays;


/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Configuration
@EnableScheduling
@Slf4j
public class Sdist002ScheduleConfig implements SchedulingConfigurer {

    private static final int POOL_SIZE = 4;

    private final String cronSchedule;
    private final String jiraSchedule;
    private final Sdist002Service sdist002Service;


    public Sdist002ScheduleConfig(@Value("${scheduler_interval_cron}") String cronScheduler,
                                  @Value("${scheduler_jira_cron}") String jiraSchedule,
                                  Sdist002Service sdist002Service) {
        this.cronSchedule = cronScheduler;
        this.sdist002Service = sdist002Service;
        this.jiraSchedule = jiraSchedule;

    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

        taskScheduler.setPoolSize(POOL_SIZE);
        taskScheduler.setThreadNamePrefix("dokdistavstemming-scheduled-task-pool-");
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.initialize();

        scheduledTaskRegistrar.setTaskScheduler(taskScheduler);
        scheduledTaskRegistrar.addCronTask(() ->
                sdist002Service.oppretteAvstemmingForsendelseJiraSakByDistribusjonKanal(), jiraSchedule);
        scheduledTaskRegistrar.addCronTask(()-> Arrays.stream(DistribusjonKanalCode.values())
                .forEach(kanal-> sdist002Service.getAvstemmForsendelseByDistKanal(kanal.name())),cronSchedule);
    }


}
