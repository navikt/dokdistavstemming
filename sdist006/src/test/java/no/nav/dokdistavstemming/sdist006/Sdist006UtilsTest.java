package no.nav.dokdistavstemming.sdist006;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.Month.AUGUST;
import static no.nav.dokdistavstemming.sdist006.Sdist006utils.determineEkspedertTil;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Sdist006UtilsTest {

	LocalDateTime friday1PM = ZonedDateTime.of(LocalDateTime.of(2023, AUGUST, 18, 13, 0), ZoneId.systemDefault()).toLocalDateTime();
	LocalDateTime friday4PM = friday1PM.plusHours(3);
	LocalDateTime saturday1PM = friday1PM.plusDays(1);
	LocalDateTime sunday1PM = friday1PM.plusDays(2);
	LocalDateTime monday7PM = friday1PM.plusDays(3).plusHours(6);

	@Test
	public void assertStartDateIsCorrect() {
		System.out.println(LocalDateTime.now().getDayOfWeek());
		//assert at dato / tidspunkt for start er riktig
		assertThat(friday1PM.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
		assertThat(friday1PM.getHour()).isEqualTo(13);
	}

	@Test
	public void shouldReturnDateWhenDateIsNotWeekend() {
		//Hvis dato er utenfor helg, returner dato
		assertThat(determineEkspedertTil(friday1PM)).isEqualTo(friday1PM);
		assertThat(determineEkspedertTil(monday7PM)).isEqualTo(monday7PM);
	}

	@Test
	public void shouldReturnFriday4PMWhenDateIsWeekend() {
		assertThat(determineEkspedertTil(saturday1PM)).isEqualTo(friday4PM);
		assertThat(determineEkspedertTil(sunday1PM)).isEqualTo(friday4PM);
	}

}
