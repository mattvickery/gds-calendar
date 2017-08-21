package com.gds.calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 08/08/2017
 */
public class LocalDateCalendarTest {

    private static final int YEAR = 2018;
    private static final int MONTH = 12;
    private static final int DAY = 30;
    private final LocalDate startDate = LocalDate.of(YEAR, MONTH, DAY);
    private int duration = 365 * 2;
    private LocalDateCalendar calendar;

    @Before
    public void before() throws Exception {
        calendar = new LocalDateCalendar(startDate, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void monthBefore_nullArgument() throws Exception {
        calendar.getLastDayOfMonthBefore(null);
    }

    @Test
    public void monthBefore_standard() throws Exception {
        final LocalDate monthBeforeStartDate = calendar.getLastDayOfMonthBefore(startDate);
        assertThat(monthBeforeStartDate.getYear(), is(2018));
        assertThat(monthBeforeStartDate.getMonthValue(), is(11));
        assertThat(monthBeforeStartDate.getDayOfMonth(), is(30));
    }

    @Test
    public void monthBefore_previousMonthShorter() throws Exception {
        final LocalDate monthBeforeStartDate = calendar.getLastDayOfMonthBefore(LocalDate.of(2017, 7, 31));
        assertThat(monthBeforeStartDate.getYear(), is(2017));
        assertThat(monthBeforeStartDate.getMonthValue(), is(6));
        assertThat(monthBeforeStartDate.getDayOfMonth(), is(30));
    }

    @Test
    public void monthBefore_previousMonthRemovedDays() throws Exception {
        calendar.remove(LocalDate.of(2017, 6, 30));
        final LocalDate monthBeforeStartDate = calendar.getLastDayOfMonthBefore(LocalDate.of(2017, 7, 31));
        assertThat(monthBeforeStartDate.getYear(), is(2017));
        assertThat(monthBeforeStartDate.getMonthValue(), is(6));
        assertThat(monthBeforeStartDate.getDayOfMonth(), is(29));
    }

    // // TODO: 08/08/2017 improve this with JUnit 5 exception testing.
    @Test (expected = IllegalStateException.class)
    public void monthBefore_outsideCalendarRange() throws Exception {
        calendar.getLastDayOfMonthBefore(LocalDate.of(2019, 7, 31));
    }
}