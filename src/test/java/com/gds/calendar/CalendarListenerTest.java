package com.gds.calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.gds.calendar.CalendarChangeEvent.DATE_ADDED;
import static com.gds.calendar.CalendarChangeEvent.DATE_REMOVED;
import static com.gds.calendar.CalendarChangeEvent.DAY_OF_WEEK_REMOVED;
import static org.hamcrest.CoreMatchers.equalTo;
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
    private final List<ChangeEventContext> eventContexts = new ArrayList<>();

    @Before
    public void before() throws Exception {
        eventContexts.clear();
        calendar = new LocalDateCalendar(startDate, "default", duration);
    }

    @Test
    public void remove_callback_fromExisting() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        calendar.register(eventContexts:: add);
        calendar.remove(targetDate);
        assertThis(eventContexts.get(0), "Date removed from calendar.", calendar, DATE_REMOVED, targetDate);
    }

    @Test
    public void remove_callback_fromNonExisting() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        calendar.remove(targetDate);
        calendar.register(eventContexts:: add);
        try {
            calendar.remove(targetDate);
        } catch (Exception e) {}
        assertThat(eventContexts.size(), is(0));
    }

    @Test
    public void add_callback_fromRemoved() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        calendar.remove(targetDate);
        calendar.register(eventContexts:: add);
        calendar.add(targetDate);
        assertThis(eventContexts.get(0), "New date added to calendar.", calendar, DATE_ADDED, targetDate);
    }

    @Test
    public void add_callback_fromExisting() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        calendar.register(eventContexts:: add);
        calendar.add(targetDate);
        assertThat(eventContexts.size(), is(0));
    }

    @Test
    public void removeWeekendDays_callback() {

        calendar.register(eventContexts:: add);
        calendar.removeWeekendDays();
        assertThis(eventContexts.get(0), "Day of Week removed [SATURDAY]", calendar, DAY_OF_WEEK_REMOVED);
        assertThis(eventContexts.get(1), "Day of Week removed [SUNDAY]", calendar, DAY_OF_WEEK_REMOVED);
        assertThis(eventContexts.get(2), "All weekend dates have been removed from calendar.",
                calendar, DAY_OF_WEEK_REMOVED);
    }

    private void assertThis(final ChangeEventContext eventContext, final String message,
                            final LocalDateCalendar calendar, final CalendarChangeEvent changeEvent,
                            final LocalDate... dates) {

        assertThat(eventContext.getCalendarChangeEvent(), equalTo(changeEvent));
        assertThat(eventContext.getCalendar(), equalTo(calendar));
        assertThat(eventContext.getMessage().get(), is(message));
        if (dates != null)
            assertThat(eventContext.getDates().toArray(), equalTo(dates));
    }
}