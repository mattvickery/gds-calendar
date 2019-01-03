package com.gds.calendar;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.gds.calendar.CalendarChangeEvent.CALENDAR_ADDED;
import static com.gds.calendar.CalendarChangeEvent.CALENDAR_REMOVED;
import static com.gds.calendar.CalendarChangeEvent.DATES_REMOVED;
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
    private final LocalDate endDate = LocalDate.of(YEAR, MONTH, DAY);
    private int duration = 365 * 2;
    private LocalDateCalendar calendar;
    private final List<ChangeEventContext> eventContexts = new ArrayList<>();

    @Before
    public void before() throws Exception {
        eventContexts.clear();
        calendar = new LocalDateCalendar(endDate, "default", duration);
    }

    @Test
    public void remove_callback_fromExisting() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        calendar.register(eventContexts::add);
        calendar.remove(targetDate);
        assertThis(eventContexts.get(0), "Date removed from calendar.", calendar, DATE_REMOVED, targetDate);
    }

    @Test
    public void remove_callback_fromNonExisting() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        calendar.remove(targetDate);
        calendar.register(eventContexts::add);
        try {
            calendar.remove(targetDate);
        } catch (Exception e) {
        }
        assertThat(eventContexts.size(), is(0));
    }

    @Test
    public void add_callback_fromRemoved() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        calendar.remove(targetDate);
        calendar.register(eventContexts::add);
        calendar.add(targetDate);
        assertThis(eventContexts.get(0), "New date added to calendar.", calendar, DATE_ADDED, targetDate);
    }

    @Test
    public void add_callback_fromExisting() {

        final LocalDate targetDate = LocalDate.of(2017, 1, 1);
        calendar.register(eventContexts::add);
        calendar.add(targetDate);
        assertThat(eventContexts.size(), is(0));
    }

    @Test
    public void removeWeekendDays_callback() {

        calendar.register(eventContexts::add);
        calendar.removeWeekendDays();
        assertThis(eventContexts.get(0), "Day of Week removed [SATURDAY]", calendar, DAY_OF_WEEK_REMOVED);
        assertThis(eventContexts.get(1), "Day of Week removed [SUNDAY]", calendar, DAY_OF_WEEK_REMOVED);
        assertThis(eventContexts.get(2), "All weekend dates have been removed from calendar.",
                calendar, DAY_OF_WEEK_REMOVED);
    }

    @Test
    public void removeWeekdays_callback() {

        calendar.register(eventContexts::add);
        calendar.removeWeekDays();
        assertThis(eventContexts.get(0), "Day of Week removed [MONDAY]", calendar, DAY_OF_WEEK_REMOVED);
        assertThis(eventContexts.get(1), "Day of Week removed [TUESDAY]", calendar, DAY_OF_WEEK_REMOVED);
        assertThis(eventContexts.get(2), "Day of Week removed [WEDNESDAY]", calendar, DAY_OF_WEEK_REMOVED);
        assertThis(eventContexts.get(3), "Day of Week removed [THURSDAY]", calendar, DAY_OF_WEEK_REMOVED);
        assertThis(eventContexts.get(4), "Day of Week removed [FRIDAY]", calendar, DAY_OF_WEEK_REMOVED);
        assertThis(eventContexts.get(5), "All weekday dates have been removed from calendar.",
                calendar, DAY_OF_WEEK_REMOVED);
    }

    @Test
    public void removeAll_callback_singleDate() {

        final LocalDate firstRemoval = LocalDate.of(2017, 7, 5);
        assertThat(calendar.getDay(firstRemoval).isPresent(), is(true));
        final List<LocalDate> toRemove = new ArrayList<LocalDate>() {{
            add(firstRemoval);
        }};
        calendar.register(eventContexts::add);
        calendar.removeAll(toRemove);

        assertThat(calendar.getDay(firstRemoval).isPresent(), is(false));
        assertThat(eventContexts.size(), is(1));
        assertThis(eventContexts.get(0), "Collection of dates removed from calendar.", calendar,
                DATES_REMOVED, toRemove.toArray(new LocalDate[]{}));

    }

    @Test
    public void removeAll_callbackMultipleDate() {

        final LocalDate firstRemoval = LocalDate.of(2017, 7, 5);
        final LocalDate secondRemoval = LocalDate.of(2018, 1, 1);
        assertThat(calendar.getDay(firstRemoval).isPresent(), is(true));
        assertThat(calendar.getDay(secondRemoval).isPresent(), is(true));
        final List<LocalDate> toRemove = new ArrayList<LocalDate>() {{
            add(firstRemoval);
            add(secondRemoval);
        }};
        calendar.register(eventContexts::add);
        calendar.removeAll(toRemove);

        assertThat(calendar.getDay(firstRemoval).isPresent(), is(false));
        assertThat(calendar.getDay(secondRemoval).isPresent(), is(false));
        assertThat(eventContexts.size(), is(1));
        assertThis(eventContexts.get(0), "Collection of dates removed from calendar.", calendar,
                DATES_REMOVED, toRemove.toArray(new LocalDate[]{}));
    }

    @Test
    public void addCalendar() {

        final LocalDateCalendar workCalendar
                = new LocalDateCalendar(endDate, "workCalendar", 7).removeWeekendDays();
        final LocalDateCalendar overtimeCalendar
                = new LocalDateCalendar(endDate, "overtimeCalendar", 21).removeWeekendDays();
        overtimeCalendar.register(eventContexts::add);
        overtimeCalendar.add(workCalendar);
        assertThis(eventContexts.get(0), "Calendar dates from workCalendar added to overtimeCalendar.",
                workCalendar, CALENDAR_ADDED);
    }

    @Test
    public void removeCalendar() {

        final LocalDateCalendar workCalendar
                = new LocalDateCalendar(endDate, "workCalendar", 7).removeWeekendDays();
        final LocalDateCalendar holidayCalendar
                = new LocalDateCalendar(endDate, "holidayCalendar", 7)
                .removeWeekendDays().removeWeekDays();
        workCalendar.register(eventContexts::add);
        workCalendar.remove(holidayCalendar);
        assertThis(eventContexts.get(0), "Calendar dates from holidayCalendar removed from workCalendar.",
                holidayCalendar, CALENDAR_REMOVED);
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