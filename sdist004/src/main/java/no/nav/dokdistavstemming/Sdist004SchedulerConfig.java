package no.nav.dokdistavstemming;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
		Sdist004Scheduler.class
})
@Configuration
public class Sdist004SchedulerConfig {
}
