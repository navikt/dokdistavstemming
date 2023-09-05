package no.nav.dokdistavstemming;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.Month.AUGUST;
import static no.nav.dokdistavstemming.consumer.dokdistadmin.Rdist001administrerforsendelseConsumer.HENTFORSENDELSER_MAX_JOURNALPOSTS;
import static no.nav.dokdistavstemming.utils.Sdist006utils.determineEkspedertTil;
import static no.nav.dokdistavstemming.utils.Sdist006utils.partitionList;
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

	@Test
	public void shouldPartitionListIntoNSize(){
		int listSize = 255;
		List<String> stringList = new ArrayList<>(listSize);
		for(int i = 0; i < listSize; i++){
			stringList.add(""+i);
		}

		List<List<String>> listOfLists = partitionList(stringList, HENTFORSENDELSER_MAX_JOURNALPOSTS);
		assertThat(listOfLists.size()).isEqualTo(2);
		assertThat(listOfLists.get(0).size()).isEqualTo(HENTFORSENDELSER_MAX_JOURNALPOSTS);
		assertThat(listOfLists.get(1).size()).isEqualTo(listSize-HENTFORSENDELSER_MAX_JOURNALPOSTS);
	}

}
