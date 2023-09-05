package no.nav.dokdistavstemming.utils;

import com.google.common.collect.Lists;

import java.time.LocalDateTime;
import java.util.List;

public class Sdist006utils {

	public static LocalDateTime determineEkspedertTil(LocalDateTime ekspedertTil) {
		return switch (ekspedertTil.getDayOfWeek()) {
			case SATURDAY -> setKlokkeslettTil16(ekspedertTil.minusDays(1));
			case SUNDAY -> setKlokkeslettTil16(ekspedertTil.minusDays(2));
			default -> ekspedertTil;
		};
	}

	public static <T> List<List<T>> partitionList(List<T> list, int partitionSize) {
		return Lists.partition(list, partitionSize);
	}

	private static LocalDateTime setKlokkeslettTil16(LocalDateTime date) {
		return date.withHour(16).withMinute(0).withSecond(0);
	}
}
