package com.gds.calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static com.gds.calendar.CalendarChangeEvent.DATE_ADDED;
import static com.gds.calendar.CalendarChangeEvent.DATE_REMOVED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 21/08/2017
 */
public class CalendarListenerTest {

    private static final int YEAR = 2018;
    private static final int MONTH = 12;
    private static final int DAY = 30;
    private final LocalDate startDate = LocalDate.of(YEAR, MONTH, DAY);
    private int duration = 365 * 2;
    private LocalDateCalendar calendar;

    @Before
    public void before() throws Exception {
        calendar = new LocalDateCalendar(startDate, "default", duration);
    }

    @Test
    public void remove_callback_fromExisting() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        final boolean [] results = new boolean[]{false};
        calendar.register((event, context) -> {
            if (event.equals(DATE_REMOVED))
                results[0] = true;
        });
        calendar.remove(targetDate);
        assertThat(results[0], is(true));
    }

    @Test
    public void remove_callback_fromNonExisting() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        final boolean [] results = new boolean[]{false};
        calendar.remove(targetDate);
        calendar.register((event, context) -> {
            if (event.equals(DATE_REMOVED))
                results[0] = true;
        });
        try {
            calendar.remove(targetDate);
        } catch(Exception e) {}
        assertThat(results[0], is(false));
    }

    @Test
    public void add_callback_fromRemoved() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        calendar.remove(targetDate);
        final boolean [] results = new boolean[]{false};
        calendar.register((event, context) -> {
            if (event.equals(DATE_ADDED))
                results[0] = true;
        });
        calendar.add(targetDate);
        assertThat(results[0], is(true));
    }

    @Test
    public void add_callback_fromExisting() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        final boolean [] results = new boolean[]{false};
        calendar.register((event, context) -> {
            if (event.equals(DATE_ADDED))
                results[0] = true;
        });
        calendar.add(targetDate);
        assertThat(results[0], is(false));
    }
}