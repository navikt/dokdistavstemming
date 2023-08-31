package no.nav.dokdistavstemming.utils;

import java.time.LocalDateTime;

public class DateUtils {

	public static LocalDateTime determineEkspedertTil(LocalDateTime ekspedertTil){
		return switch (ekspedertTil.getDayOfWeek()) {
			case SATURDAY -> setKlokkeslettTil16(ekspedertTil.minusDays(1));
			case SUNDAY -> setKlokkeslettTil16(ekspedertTil.minusDays(2));
			default -> ekspedertTil;
		};
	}

	private static LocalDateTime setKlokkeslettTil16(LocalDateTime date) {
		return date.withHour(16).withMinute(0).withSecond(0);
	}
}
