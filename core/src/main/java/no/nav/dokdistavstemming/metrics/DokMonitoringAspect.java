package no.nav.dokdistavstemming.metrics;


import io.micrometer.core.annotation.Incubating;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.lang.NonNullApi;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistavstemming.mdc.MDCConstants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.function.Function;

import static no.nav.dokdistavstemming.metrics.MetricUtils.isFunctionalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */

@Aspect
@Incubating(since = "1.0.0")
@NonNullApi
@Slf4j
public class DokMonitoringAspect {

	private final MeterRegistry registry;
	private final Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinpoint;

	public DokMonitoringAspect(MeterRegistry registry) {
		this(registry, pjp ->
				Tags.of("class", pjp.getStaticPart().getSignature().getDeclaringTypeName(),
						"method", pjp.getStaticPart().getSignature().getName())
		);
	}

	public DokMonitoringAspect(MeterRegistry registry, Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinpoint) {
		this.registry = registry;
		this.tagsBasedOnJoinpoint = tagsBasedOnJoinpoint;
	}

	@Around("execution (@no.nav.dokdistavstemming.metrics.Monitor * *.*(..))")
	public Object timedMethod(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		Monitor monitor = method.getAnnotation(Monitor.class);

		if (monitor.value().isEmpty()) {
			return pjp.proceed();
		}

		Timer.Sample sample = Timer.start(registry);
		try {
			return pjp.proceed();
		} catch (Exception e) {

			if (monitor.logException()) {
				logException(e);
			}

			Counter.builder(monitor.value() + "_exception")
					.tags("error_type", isFunctionalException(e) ? "functional" : "technical")
					.tags("exception_name", e.getClass().getSimpleName())
					.tags(monitor.extraTags())
					.tags(tagsBasedOnJoinpoint.apply(pjp))
					.register(registry)
					.increment();
			throw e;
		} finally {
			sample.stop(Timer.builder(monitor.value())
					.description(monitor.description().isEmpty() ? null : monitor.description())
					.tags(monitor.extraTags())
					.tags(tagsBasedOnJoinpoint.apply(pjp))
					.publishPercentileHistogram(monitor.histogram())
					.publishPercentiles(monitor.percentiles().length == 0 ? null : monitor.percentiles())
					.register(registry));
		}
	}

	private void logException(Exception e) {
		if (isFunctionalException(e)) {
			log.warn(MDC.get(MDCConstants.MDC_REQUEST_ID) + " " + e.getMessage());
		} else {
			log.error(MDC.get(MDCConstants.MDC_REQUEST_ID) + " " + e.getMessage(), e);
		}
	}
}
