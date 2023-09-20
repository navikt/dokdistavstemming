package no.nav.dokdistavstemming.utils;

import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.UUID;

import static no.nav.dokdistavstemming.constants.MDCConstants.MDC_CALL_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Sdist006utils {

	public final static String DOKDISTDITTNAV = "dokdistdittnav";
	public static LocalDateTime determineEkspedertTil(LocalDateTime ekspedertTil) {
		return switch (ekspedertTil.getDayOfWeek()) {
			case SATURDAY -> setKlokkeslettTil16(ekspedertTil.minusDays(1));
			case SUNDAY -> setKlokkeslettTil16(ekspedertTil.minusDays(2));
			default -> ekspedertTil;
		};
	}

	private static LocalDateTime setKlokkeslettTil16(LocalDateTime date) {
		return date.withHour(16).withMinute(0).withSecond(0);
	}


	public static String getCallId() {
		final String callId = MDC.get(MDC_CALL_ID);
		return isBlank(callId) ? UUID.randomUUID().toString() : callId;
	}
}
