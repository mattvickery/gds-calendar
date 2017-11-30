package com.gds.calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gds.calendar.CalendarChangeEvent.CALENDAR_ADDED;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.AUGUST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 08/08/2017
 */
public class LocalDateCalendarTest {

    private static final int YEAR = 2018;
    private static final int MONTH = 12;
    private static final int DAY = 30;
    private final LocalDate endDate = LocalDate.of(YEAR, MONTH, DAY);
    private final int duration = 365 * 2;

    private final LocalDateCalendar weekdayCalendar
            = new LocalDateCalendar(endDate, "weekday", duration).removeWeekendDays();
    private final LocalDateCalendar weekendCalendar
            = new LocalDateCalendar(endDate, "weekend", duration).removeWeekDays();
    private LocalDateCalendar calendar;

    @Before
    public void before() throws Exception {
        calendar = new LocalDateCalendar(endDate, "default", duration);
    }

    @Test
    public void simpleConstructor() {

        calendar = new LocalDateCalendar(endDate);

        assertThat(calendar.getName(), is("default"));
        assertThat(calendar.getStartDate(), is(endDate.minusDays(364)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void monthBefore_nullArgument() throws Exception {

        calendar.getLastDayOfMonthBefore(null);
    }

    @Test
    public void monthBefore_standard() throws Exception {

        final LocalDate monthBeforeEndDate = calendar.getLastDayOfMonthBefore(endDate).get();

        assertThat(monthBeforeEndDate.getYear(), is(2018));
        assertThat(monthBeforeEndDate.getMonthValue(), is(11));
        assertThat(monthBeforeEndDate.getDayOfMonth(), is(30));
    }

    @Test
    public void monthBefore_previousMonthShorter() throws Exception {

        final LocalDate monthBeforeEndDate = calendar.getLastDayOfMonthBefore(LocalDate.of(2017, 7, 31)).get();

        assertThat(monthBeforeEndDate.getYear(), is(2017));
        assertThat(monthBeforeEndDate.getMonthValue(), is(6));
        assertThat(monthBeforeEndDate.getDayOfMonth(), is(30));
    }

    @Test
    public void monthBefore_previousMonthRemovedDays() throws Exception {

        calendar.remove(LocalDate.of(2017, 6, 30));
        final LocalDate monthBeforeEndDate = calendar.getLastDayOfMonthBefore(LocalDate.of(2017, 7, 31)).get();

        assertThat(monthBeforeEndDate.getYear(), is(2017));
        assertThat(monthBeforeEndDate.getMonthValue(), is(6));
        assertThat(monthBeforeEndDate.getDayOfMonth(), is(29));
    }

    @Test
    public void monthBefore_outsideCalendarRange() throws Exception {
        assertThat(calendar.getLastDayOfMonthBefore(LocalDate.of(2019, 7, 31)).isPresent(), is(false));
    }

    @Test
    public void getName() {
        assertThat(calendar.getName(), is("default"));
    }

    @Test
    public void startDate() {

        final LocalDate endDate = LocalDate.of(2020, 01, 15);
        final int duration = 5;
        final LocalDateCalendar calendar = new LocalDateCalendar(endDate, "default", duration);
        final LocalDate expectedStartDate = LocalDate.of(2020, 01, 11);

        assertThat(calendar.getStartDate(), is(expectedStartDate));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDayBefore_nullArgument() {
        calendar.getDayBefore(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDayBefore_emptyCalendar() {
        calendar.removeWeekDays().removeWeekendDays();
        calendar.getDayBefore(calendar.getEndDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDayBefore_beforeCalendarStarts() {
        calendar.getDayBefore(calendar.getStartDate().minusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDayBefore_afterCalendarEnds() {
        calendar.getDayBefore(calendar.getEndDate().plusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDayBefore_startDate() {
        calendar.getDayBefore(calendar.getStartDate());
    }

    @Test
    public void getDayBefore_dayAfterStartDate() {

        final LocalDate dayBefore = calendar.getDayBefore(calendar.getStartDate().plusDays(1)).get();
        assertThat(dayBefore, is(calendar.getStartDate()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDayBefore_afterEndDate() {
        calendar.getDayBefore(calendar.getEndDate().plusDays(1));
    }

    @Test
    public void getDayBefore_endDate() {

        LocalDate dayBeforeEndDate = calendar.getDayBefore(calendar.getEndDate()).get();
        assertThat(dayBeforeEndDate, equalTo(LocalDate.of(2018, 12, 29)));
        calendar.remove(LocalDate.of(2018, 12, 29));
        dayBeforeEndDate = calendar.getDayBefore(calendar.getEndDate()).get();
        assertThat(dayBeforeEndDate, equalTo(LocalDate.of(2018, 12, 28)));
    }

    @Test
    public void getDayBefore_singleDate() {
        calendar.removeWeekDays().removeWeekendDays();
        final LocalDate saturday = LocalDate.of(2017, 9, 16);
        calendar.add(saturday);
        assertThat(calendar.getDayBefore(calendar.getEndDate()).get(), equalTo(saturday));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDay_nullArgument() {
        calendar.getDay(null);
    }

    @Test
    public void getDay_beforeCalendarStart() {
        assertThat(calendar.getDay(calendar.getStartDate().minusDays(1)).isPresent(), is(false));
    }

    @Test
    public void getDay_afterCalendarStartBeforeEnd() {
        assertThat(calendar.getDay(calendar.getStartDate().plusDays(5)).isPresent(), is(true));
    }

    @Test
    public void getDay_startDate() {
        assertThat(calendar.getDay(calendar.getStartDate()).get(), is(calendar.getStartDate()));
    }

    @Test
    public void getDay_endDate() {
        assertThat(calendar.getDay(calendar.getEndDate()).get(), is(calendar.getEndDate()));
    }

    @Test
    public void getDay_afterCalendarEnd() {
        assertThat(calendar.getDay(calendar.getEndDate().plusDays(1)).isPresent(), is(false));
    }

    @Test
    public void getDay_beforeAndAfterRemoval() {

        assertThat(calendar.getDay(calendar.getStartDate()).isPresent(), is(true));
        calendar.remove(calendar.getStartDate());
        assertThat(calendar.getDay(calendar.getStartDate()).isPresent(), is(false));
    }

    @Test
    public void removeWeekendDays_beforeAndAfter() {

        final LocalDate saturday = LocalDate.of(2017, 9, 16);
        final LocalDate sunday = LocalDate.of(2017, 9, 17);
        assertThat(calendar.getDay(saturday).isPresent(), is(true));
        assertThat(calendar.getDay(sunday).isPresent(), is(true));
        calendar.removeWeekendDays();

        assertThat(calendar.getDay(saturday).isPresent(), is(false));
        assertThat(calendar.getDay(sunday).isPresent(), is(false));
    }

    @Test
    public void removeWeekdays_beforeAndAfter() {

        final LocalDate[] weekdays = new LocalDate[]{LocalDate.of(2017, 9, 11), LocalDate.of(2017, 9, 12),
                LocalDate.of(2017, 9, 13), LocalDate.of(2017, 9, 14), LocalDate.of(2017, 9, 15)};
        for (int index = 0; index < weekdays.length; index++)
            assertThat(calendar.getDay(weekdays[index]).isPresent(), is(true));
        calendar.removeWeekDays();
        for (int index = 0; index < weekdays.length; index++)
            assertThat(calendar.getDay(weekdays[index]).isPresent(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void remove_date_null() {

        final LocalDate date = null;
        calendar.remove(date);
    }

    @Test
    public void remove_date_present() {

        assertThat(calendar.getDay(calendar.getStartDate()).isPresent(), is(true));
        calendar.remove(calendar.getStartDate());
        assertThat(calendar.getDay(calendar.getStartDate()).isPresent(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void remove_date_notPresent() {
        calendar.remove(LocalDate.of(2015, 1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeAll_nullArgument() {
        calendar.removeAll(null);
    }

    @Test
    public void removeAll_allOutsideRange() {

        // todo:
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeAll_someOutsideRange_ignoreUnknownDatesFalse() {

        final LocalDate outsideCalendarRange = LocalDate.of(2010, 1, 1);
        assertThat(calendar.getDay(outsideCalendarRange).isPresent(), is(false));
        calendar.removeAll(new ArrayList<LocalDate>() {{
            add(outsideCalendarRange);
        }}, false);
    }

    @Test
    public void removeAll_someOutsideRange_ignoreUnknownDatesTrue() {

        final LocalDate outsideCalendarRange = LocalDate.of(2010, 1, 1);
        assertThat(calendar.getDay(outsideCalendarRange).isPresent(), is(false));
        calendar.removeAll(new ArrayList<LocalDate>() {{
            add(outsideCalendarRange);
        }}, true);
        assertThat(calendar.getDay(outsideCalendarRange).isPresent(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeAll_someOutsideRange_ignoreUnknownDatesDefault() {

        final LocalDate outsideCalendarRange = LocalDate.of(2010, 1, 1);
        assertThat(calendar.getDay(outsideCalendarRange).isPresent(), is(false));
        calendar.removeAll(new ArrayList<LocalDate>() {{
            add(outsideCalendarRange);
        }});
    }

    @Test(expected = IllegalArgumentException.class)
    public void remove_dayOfWeek_nullArgument() {

        final DayOfWeek dayOfWeek = null;
        calendar.remove(dayOfWeek);
    }

    @Test
    public void remove_dayOfWeek_weekendDayOfWeek() {

        final LocalDate saturday = LocalDate.of(2017, 9, 16);
        final LocalDate sunday = LocalDate.of(2017, 9, 17);
        assertThat(calendar.getDay(saturday).isPresent(), is(true));
        assertThat(calendar.getDay(sunday).isPresent(), is(true));
        calendar.remove(SUNDAY);

        assertThat(calendar.getDay(saturday).isPresent(), is(true));
        assertThat(calendar.getDay(sunday).isPresent(), is(false));
    }

    @Test
    public void remove_dayOfWeek_alreadyRemoved() {

        final LocalDate saturday = LocalDate.of(2017, 9, 16);
        final LocalDate sunday = LocalDate.of(2017, 9, 17);
        assertThat(calendar.getDay(saturday).isPresent(), is(true));
        assertThat(calendar.getDay(sunday).isPresent(), is(true));
        calendar.remove(SUNDAY);
        calendar.remove(SUNDAY);

        assertThat(calendar.getDay(saturday).isPresent(), is(true));
        assertThat(calendar.getDay(sunday).isPresent(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void remove_calendar_null() {
        final LocalDateCalendar nullCalendar = null;
        calendar.remove(nullCalendar);
    }

    @Test
    public void remove_calendar_empty() {

        final List<LocalDate> allDatesBefore = new ArrayList<>(calendar.getAllDates());
        calendar.remove(LocalDateCalendar.empty());
        final List<LocalDate> allDatesAfter = new ArrayList<>(calendar.getAllDates());

        assertThat(allDatesBefore, equalTo(allDatesAfter));
    }

    @Test(expected = IllegalStateException.class)
    public void remove_calendar_fromItself() {
        calendar.remove(calendar);
    }

    @Test
    public void remove_calendar_duplicateCalendar() {

        final List<LocalDate> allDatesBefore = new ArrayList<>(calendar.getAllDates());
        final LocalDateCalendar duplicateCalendar = new LocalDateCalendar(endDate, "duplicate", duration);
        calendar.remove(duplicateCalendar);
        final List<LocalDate> allDatesAfter = new ArrayList<>(calendar.getAllDates());

        assertThat(allDatesBefore, not(equalTo(allDatesAfter)));
        assertThat(allDatesBefore.size(), is(730));
        assertThat(allDatesAfter.size(), is(0));
    }

    @Test
    public void remove_calendar_overlappingDates() {

        final LocalDateCalendar workCalendar
                = new LocalDateCalendar(endDate, "workCalendar", 21);
        final LocalDateCalendar overtimeCalendar
                = new LocalDateCalendar(endDate, "overtimeCalendar", 7);
        assertThat(workCalendar.getAllDates().size(), is(21));
        assertThat(overtimeCalendar.getAllDates().size(), is(7));

        workCalendar.remove(overtimeCalendar);
        assertThat(workCalendar.getAllDates().size(), is(14));
        assertThat(overtimeCalendar.getAllDates().size(), is(7));
    }

    @Test
    public void remove_calendar_noOverlappingDates() {

        final List<LocalDate> allDatesBefore = new ArrayList<>(calendar.getAllDates());
        weekdayCalendar.remove(weekendCalendar);
        final List<LocalDate> allDatesAfter = new ArrayList<>(calendar.getAllDates());

        assertThat(allDatesBefore, equalTo(allDatesAfter));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_calendar_null() {
        final LocalDateCalendar localDateCalendar = null;
        calendar.add(localDateCalendar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_date_null() {
        final LocalDate nullDate = null;
        calendar.add(nullDate);
    }

    @Test
    public void add_date_notPresent() {

        final LocalDate date = LocalDate.of(2017, 9, 18);
        final int sizePreChange = calendar.getAllDates().size();
        calendar.add(date);
        int sizePostChange = calendar.getAllDates().size();
        assertThat(sizePreChange, is(sizePostChange));
        calendar.remove(date);
        sizePostChange = calendar.getAllDates().size();
        assertThat(sizePreChange, not(is(sizePostChange)));
        calendar.add(date);
        sizePostChange = calendar.getAllDates().size();
        assertThat(sizePreChange, is(sizePostChange));
        assertThat(calendar.getDay(date).get(), equalTo(date));
    }

    @Test
    public void add_date_duplicate() {

        final LocalDate duplicateDate = LocalDate.of(2017, 9, 18);
        final LocalDate datePreChange = calendar.getDay(duplicateDate).get();
        final int sizePreChange = calendar.getAllDates().size();
        calendar.add(duplicateDate);
        final LocalDate datePostChange = calendar.getDay(duplicateDate).get();
        final int sizePostChange = calendar.getAllDates().size();

        assertThat(sizePreChange, is(sizePostChange));
        // Check object identifiers are the same.
        assertThat(datePreChange == datePostChange, is(true));
    }

    @Test(expected = IllegalStateException.class)
    public void add_date_beforeStart() {
        final LocalDate date = LocalDate.of(2010, 9, 18);
        calendar.add(date);
    }

    @Test(expected = IllegalStateException.class)
    public void add_date_afterEnd() {
        final LocalDate date = LocalDate.of(2020, 9, 18);
        calendar.add(date);
    }

    @Test
    public void add_date_toEmpty_addStartDate() {

        final LocalDate endDate = LocalDate.of(2020, 9, 13);
        calendar = new LocalDateCalendar(endDate, "tester", 7);
        calendar.removeWeekendDays().removeWeekDays();
        final LocalDate monday = LocalDate.of(2020, 9, 7);

        assertThat(calendar.getAllDates().size(), is(0));
        calendar.add(monday);
        assertThat(calendar.getAllDates().size(), is(1));
    }

    @Test
    public void add_date_toEmpty_addEndDate() {

        final LocalDate endDate = LocalDate.of(2020, 9, 13);
        calendar = new LocalDateCalendar(endDate, "tester", 7);
        calendar.removeWeekendDays().removeWeekDays();

        assertThat(calendar.getAllDates().size(), is(0));
        calendar.add(endDate);
        assertThat(calendar.getAllDates().size(), is(1));
    }

    @Test
    public void add_date_inMiddleOfEmptyCalendar() {

        final LocalDate endDate = LocalDate.of(2020, 9, 13);
        calendar = new LocalDateCalendar(endDate, "tester", 7);
        calendar.removeWeekendDays().removeWeekDays();
        final LocalDate wednesday = LocalDate.of(2020, 9, 9);

        assertThat(calendar.getAllDates().size(), is(0));
        calendar.add(wednesday);
        assertThat(calendar.getAllDates().size(), is(1));
    }

    @Test
    public void add_date_inWideGap() {

        final LocalDate endDate = LocalDate.of(2020, 9, 13);
        calendar = new LocalDateCalendar(endDate, "tester", 7);
        calendar.remove(TUESDAY).remove(WEDNESDAY).remove(THURSDAY).remove(FRIDAY).remove(SATURDAY);
        final LocalDate wednesday = LocalDate.of(2020, 9, 9);

        assertThat(calendar.getAllDates().size(), is(2));
        calendar.add(wednesday);
        assertThat(calendar.getAllDates().size(), is(3));
    }

    @Test
    public void add_dates_betweenStartOfCalendarAndFirstDate() {

        final LocalDate endDate = LocalDate.of(2020, 9, 13);
        calendar = new LocalDateCalendar(endDate, "tester", 7);
        calendar.remove(MONDAY).remove(TUESDAY).remove(WEDNESDAY);
        final LocalDate monday = LocalDate.of(2020, 9, 7);
        final LocalDate tuesday = LocalDate.of(2020, 9, 8);
        final LocalDate wednesday = LocalDate.of(2020, 9, 9);

        assertThat(calendar.getAllDates().size(), is(4));
        calendar.add(monday);
        assertThat(calendar.getAllDates().size(), is(5));
        calendar.add(tuesday);
        assertThat(calendar.getAllDates().size(), is(6));
        calendar.add(wednesday);
        assertThat(calendar.getAllDates().size(), is(7));
    }

    @Test
    public void add_date_betweenEndOfCalendarAndLastDate() {

        final LocalDate endDate = LocalDate.of(2020, 9, 13);
        calendar = new LocalDateCalendar(endDate, "tester", 7);
        calendar.remove(THURSDAY).remove(FRIDAY).remove(SATURDAY).remove(SUNDAY);

        final LocalDate sunday = LocalDate.of(2020, 9, 13);
        final LocalDate saturday = LocalDate.of(2020, 9, 12);
        final LocalDate friday = LocalDate.of(2020, 9, 11);
        final LocalDate thursday = LocalDate.of(2020, 9, 10);

        assertThat(calendar.getAllDates().size(), is(3));
        calendar.add(sunday);
        assertThat(calendar.getAllDates().size(), is(4));
        calendar.add(saturday);
        assertThat(calendar.getAllDates().size(), is(5));
        calendar.add(friday);
        assertThat(calendar.getAllDates().size(), is(6));
        calendar.add(thursday);
        assertThat(calendar.getAllDates().size(), is(7));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDatesForDayOfWeek_null() {
        calendar.getDatesForDaysOfWeek(null);
    }

    @Test
    public void getDatesForDayOfWeek_saturday() {
        final List<LocalDate> saturdays = calendar.getDatesForDaysOfWeek(SATURDAY);
        assertThat(saturdays.stream()
                .map(LocalDate::getDayOfWeek)
                .anyMatch(dayOfWeek -> !dayOfWeek.equals(SATURDAY)), is(false));
    }

    @Test (expected = IllegalArgumentException.class)
    public void isFirstDayInTheMonth_null() {
        calendar.isFirstDayInTheMonth(null);
    }

    @Test
    public void isFirstDayInTheMonth_outsideCalendarRangeAfter() {
        final LocalDate testDate = LocalDate.of(2020, 9, 1);
        assertThat(calendar.isFirstDayInTheMonth(testDate), is(false));
    }

    @Test
    public void isFirstDayInTheMonth_outsideCalendarRangeBefore() {
        final LocalDate testDate = LocalDate.of(2016, 9, 1);
        assertThat(calendar.isFirstDayInTheMonth(testDate), is(false));
    }

    @Test
    public void isFirstDayInTheMonth_insideCalendarRange() {
        final LocalDate testDate = LocalDate.of(2017, 9, 1);
        assertThat(calendar.isFirstDayInTheMonth(testDate), is(true));
    }

    @Test
    public void isFirstDayInTheMonth_insideCalendarRange_2() {
        final LocalDate testDate = LocalDate.of(2017, 9, 2);
        assertThat(calendar.isFirstDayInTheMonth(testDate), is(false));
    }

    @Test
    public void isFirstDayInTheMonth_insideCalendarRange_firstDayRemoved() {
        final LocalDate testDate = LocalDate.of(2017, 9, 2);
        calendar.remove(testDate.minusDays(1));
        assertThat(calendar.isFirstDayInTheMonth(testDate), is(true));
    }

    @Test (expected = IllegalArgumentException.class)
    public void getFirstDayInTheMonth_null_1() {
        calendar.getFirstDayOfTheMonth(null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void getFirstDayInTheMonth_null_2() {
        calendar.getFirstDayOfTheMonth(Year.of(2017), null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void getFirstDayInTheMonth_null_3() {
        calendar.getFirstDayOfTheMonth(null, AUGUST);
    }

    @Test
    public void getFirstDayInTheMonth_insideCalendarRange() {
        Optional<LocalDate> date = calendar.getFirstDayOfTheMonth(Year.of(2017), AUGUST);
        assertThat(date.get(), equalTo(LocalDate.of(2017,8,1)));
    }

    @Test
    public void getFirstDayInTheMonth_insideCalendarRange_firstDayRemoved() {
        calendar.remove(LocalDate.of(2017,8,1));
        Optional<LocalDate> date = calendar.getFirstDayOfTheMonth(Year.of(2017), AUGUST);
        assertThat(date.get(), equalTo(LocalDate.of(2017,8,2)));
    }

    @Test
    public void getFirstDayInTheMonth_outsideCalendarRange() {
        Optional<LocalDate> date = calendar.getFirstDayOfTheMonth(Year.of(2010), AUGUST);
        assertThat(date.isPresent(), is(false));
    }


    @Test
    public void getFirstDayInTheMonth_() {
        Optional<LocalDate> date = calendar.getFirstDayOfTheMonth(Year.of(2017), AUGUST);
        assertThat(date.get(), equalTo(LocalDate.of(2017,8,1)));
    }
}