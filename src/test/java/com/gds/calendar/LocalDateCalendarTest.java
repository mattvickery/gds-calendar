package com.gds.calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Test (expected = IllegalArgumentException.class)
    public void getDayBefore_nullArgument() {
        calendar.getDayBefore(null);
    }

    @Test (expected = IllegalStateException.class)
    public void getDayBefore_beforeCalendarStarts() {
        calendar.getDayBefore(calendar.getStartDate().minusDays(1));
    }

    @Test (expected = IllegalStateException.class)
    public void getDayBefore_afterCalendarEnds() {
        calendar.getDayBefore(calendar.getEndDate().plusDays(1));
    }

    @Test (expected = IllegalStateException.class)
    public void getDayBefore_startDate() {

        final LocalDate dayBefore = calendar.getDayBefore(calendar.getStartDate()).get();
        assertThat(dayBefore, is(calendar.getStartDate()));
    }

    @Test
    public void getDayBefore_dayAfterStartDate() {

        final LocalDate dayBefore = calendar.getDayBefore(calendar.getStartDate().plusDays(1)).get();
        assertThat(dayBefore, is(calendar.getStartDate()));
    }

    @Test (expected = IllegalStateException.class)
    public void getDayBefore_endDate() {
        calendar.getDayBefore(calendar.getEndDate().plusDays(1));
    }

    @Test (expected = IllegalArgumentException.class)
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

        final LocalDate [] weekdays = new LocalDate[] {LocalDate.of(2017, 9, 11), LocalDate.of(2017, 9, 12),
                LocalDate.of(2017, 9, 13), LocalDate.of(2017, 9, 14), LocalDate.of(2017, 9, 15)};
        for (int index = 0; index < weekdays.length; index++)
            assertThat(calendar.getDay(weekdays[index]).isPresent(), is(true));
        calendar.removeWeekDays();
        for (int index = 0; index < weekdays.length; index++)
            assertThat(calendar.getDay(weekdays[index]).isPresent(), is(false));
    }

    @Test (expected = IllegalArgumentException.class)
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

    @Test (expected = IllegalArgumentException.class)
    public void remove_date_notPresent() {
        calendar.remove(LocalDate.of(2015,1,1));
    }

    @Test (expected = IllegalArgumentException.class)
    public void removeAll_nullArgument() {
        calendar.removeAll(null);
    }

    @Test
    public void removeAll_allOutsideRange() {

        // todo:
    }

    @Test (expected = IllegalArgumentException.class)
    public void removeAll_someOutsideRange_ignoreUnknownDatesFalse() {

        final LocalDate outsideCalendarRange = LocalDate.of(2010,1,1);
        assertThat(calendar.getDay(outsideCalendarRange).isPresent(), is(false));
        calendar.removeAll(new ArrayList<LocalDate>() {{
            add(outsideCalendarRange);
        }}, false);
    }

    @Test
    public void removeAll_someOutsideRange_ignoreUnknownDatesTrue() {

        final LocalDate outsideCalendarRange = LocalDate.of(2010,1,1);
        assertThat(calendar.getDay(outsideCalendarRange).isPresent(), is(false));
        calendar.removeAll(new ArrayList<LocalDate>() {{
            add(outsideCalendarRange);
        }}, true);
        assertThat(calendar.getDay(outsideCalendarRange).isPresent(), is(false));
    }

    @Test (expected = IllegalArgumentException.class)
    public void removeAll_someOutsideRange_ignoreUnknownDatesDefault() {

        final LocalDate outsideCalendarRange = LocalDate.of(2010,1,1);
        assertThat(calendar.getDay(outsideCalendarRange).isPresent(), is(false));
        calendar.removeAll(new ArrayList<LocalDate>() {{
            add(outsideCalendarRange);
        }});
    }

    @Test (expected = IllegalArgumentException.class)
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
        calendar.remove(DayOfWeek.SUNDAY);

        assertThat(calendar.getDay(saturday).isPresent(), is(true));
        assertThat(calendar.getDay(sunday).isPresent(), is(false));
    }

    @Test
    public void remove_dayOfWeek_alreadyRemoved() {

        final LocalDate saturday = LocalDate.of(2017, 9, 16);
        final LocalDate sunday = LocalDate.of(2017, 9, 17);
        assertThat(calendar.getDay(saturday).isPresent(), is(true));
        assertThat(calendar.getDay(sunday).isPresent(), is(true));
        calendar.remove(DayOfWeek.SUNDAY);
        calendar.remove(DayOfWeek.SUNDAY);

        assertThat(calendar.getDay(saturday).isPresent(), is(true));
        assertThat(calendar.getDay(sunday).isPresent(), is(false));
    }

    @Test (expected = IllegalArgumentException.class)
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

    @Test (expected = IllegalStateException.class)
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

        // todo:
    }

    @Test
    public void remove_calendar_noOverlappingDates() {

        final List<LocalDate> allDatesBefore = new ArrayList<>(calendar.getAllDates());
        weekdayCalendar.remove(weekendCalendar);
        final List<LocalDate> allDatesAfter = new ArrayList<>(calendar.getAllDates());

        assertThat(allDatesBefore, equalTo(allDatesAfter));
    }

    @Test
    public void add_calendar_null() {

    }

    @Test (expected = IllegalArgumentException.class)
    public void add_date_null() {

        final LocalDate nullDate = null;
        calendar.add(nullDate);
    }

    @Test
    public void add_date_notPresent() {

        final LocalDate date = LocalDate.of(2018,1,1);

    }

    @Test
    public void add_date_duplicate() {

    }

    @Test
    public void add_date_beforeStart() {

    }

    @Test
    public void add_date_afterEnd() {

    }

    @Test
    public void add_date_inWideGap() {

    }

    @Test
    public void add_date_inMiddleOfEmptyCalendar() {

    }
}