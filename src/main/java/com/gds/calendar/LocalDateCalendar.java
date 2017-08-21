package com.gds.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gds.calendar.CalendarChangeEvent.CALENDAR_ADDED;
import static com.gds.calendar.CalendarChangeEvent.CALENDAR_REMOVED;
import static com.gds.calendar.CalendarChangeEvent.DATES_REMOVED;
import static com.gds.calendar.CalendarChangeEvent.DATE_ADDED;
import static com.gds.calendar.CalendarChangeEvent.DATE_REMOVED;
import static com.gds.calendar.CalendarChangeEvent.INITIALISED;
import static com.gds.calendar.CalendarChangeEvent.WEEKDAY_REMOVED;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 08/08/2017
 */
public class LocalDateCalendar {

    private static final int DEF_CALENDAR_PERIOD = 365;
    private static final String MANDATORY_ARGUMENT_DATE_IS_MISSING = "Mandatory argument 'date' is missing";
    private final List<LocalDate> days = new LinkedList<>();
    private final LocalDate endDate;
    private final int calendarPeriod;
    private final List<LocalDateCalendarListener> listenerRegistry = new ArrayList<>();

    /**
     * Create a calendar using the default calendar period defined by LocalDateCalendar.DEF_CALENDAR_PERIOD.
     *
     * @param endDate the last date in the calendar.
     */
    public LocalDateCalendar(final LocalDate endDate) {
        this(endDate, DEF_CALENDAR_PERIOD);
    }

    /**
     * @param endDate
     * @param calendarPeriod
     * @param listeners
     */
    public LocalDateCalendar(final LocalDate endDate,
                             final int calendarPeriod,
                             final LocalDateCalendarListener... listeners) {

        notNull(endDate, "Mandatory argument 'endDate' is missing");
        state(calendarPeriod > 0, "Argument 'calendarPeriod' must be > 0");
        this.endDate = endDate;
        this.calendarPeriod = calendarPeriod;
        for (int index = 0; index < calendarPeriod; index++)
            days.add(index, endDate.minusDays(index));
        Arrays.asList(listeners).stream().forEach(listenerRegistry:: add);
        listenerRegistry.forEach(listener -> listener.calendarEventTriggered(INITIALISED));

    }

    /**
     * @param date
     * @return
     */
    public Optional<LocalDate> getDayBefore(final LocalDate date) {

        notNull(date, MANDATORY_ARGUMENT_DATE_IS_MISSING);
        if (days.indexOf(date) > - 1)
            return Optional.of(days.get(days.indexOf(date) + 1));
        if ((date.isAfter(endDate)) || (date.isBefore(endDate.minusDays(calendarPeriod))))
            throw new IllegalStateException("Date is outside of calendar range.");
        if (date.isBefore(days.get(days.size() - 1)))
            return Optional.empty();
        int nearestAfterIndex = 0;
        for (int index = days.size() - 1; index >= 0; index--)
            if (days.get(index).isBefore(date))
                nearestAfterIndex = index;
        return Optional.of(days.get(nearestAfterIndex));
    }

    /**
     * @param date
     * @return
     */
    public Optional<LocalDate> getDay(final LocalDate date) {

        notNull(date, MANDATORY_ARGUMENT_DATE_IS_MISSING);
        return days.stream().filter(indexDate -> date.equals(indexDate)).findFirst();
    }

    /**
     * @return
     */
    public LocalDateCalendar removeWeekendDays() {
        return remove(DayOfWeek.SATURDAY).remove(DayOfWeek.SUNDAY);
    }

    /**
     * @return
     */
    public LocalDateCalendar removeWeekDays() {
        return remove(DayOfWeek.MONDAY).remove(DayOfWeek.TUESDAY).remove(DayOfWeek.WEDNESDAY)
                .remove(DayOfWeek.THURSDAY).remove(DayOfWeek.FRIDAY);
    }

    /**
     * @param date
     * @return
     */
    public LocalDateCalendar remove(final LocalDate date) {

        notNull(date, MANDATORY_ARGUMENT_DATE_IS_MISSING);
        if (days.remove(date))
            listenerRegistry.forEach(listener -> listener.calendarEventTriggered(DATE_REMOVED));
        return this;
    }

    /**
     * @param dates
     * @return
     */
    public LocalDateCalendar removeAll(final List<LocalDate> dates) {

        notNull(dates, "Mandatory argument 'dates' is missing.");
        if (days.removeAll(dates))
            listenerRegistry.forEach(listener -> listener.calendarEventTriggered(DATES_REMOVED));
        return this;
    }

    /**
     * @param dayOfWeek
     * @return
     */
    public LocalDateCalendar remove(final DayOfWeek dayOfWeek) {

        notNull(dayOfWeek, "Mandatory argument 'dayOfWeek' is missing.");
        final List<LocalDate> matchingDayOfWeeks = days.stream()
                .filter(day -> day.getDayOfWeek().equals(dayOfWeek))
                .collect(Collectors.toList());
        if (days.removeAll(matchingDayOfWeeks))
            listenerRegistry.forEach(listener -> listener.calendarEventTriggered(WEEKDAY_REMOVED));
        return this;
    }

    /**
     * @param calendar
     * @return
     */
    public LocalDateCalendar remove(final LocalDateCalendar calendar) {

        notNull(calendar, "Mandatory argument 'calendar' is missing.");
        calendar.days.forEach(this :: remove);
        listenerRegistry.forEach(listener -> listener.calendarEventTriggered(CALENDAR_REMOVED));
        return this;
    }

    /**
     * @param calendar
     * @return
     */
    public LocalDateCalendar add(final LocalDateCalendar calendar) {

        notNull(calendar, "Mandatory argument 'calendar' is missing.");
        calendar.days.forEach(this :: add);
        listenerRegistry.forEach(listener -> listener.calendarEventTriggered(CALENDAR_ADDED));
        return this;
    }

    /**
     * @param date
     * @return
     */
    public LocalDateCalendar add(final LocalDate date) {

        notNull(date, MANDATORY_ARGUMENT_DATE_IS_MISSING);
        if (days.indexOf(date) > - 1)
            return this;
        if ((days.size() > 0) && (days.get(0).compareTo(date) < 0)) {
            days.add(0, date);
            listenerRegistry.forEach(listener -> listener.calendarEventTriggered(DATE_ADDED));
            return this;
        }
        int lastIndexExceedingDate = 0;
        for (int index = 0; index < days.size(); index++) {
            int gap = days.get(index).compareTo(date);
            if ((gap > 0) && (gap < days.get(lastIndexExceedingDate).compareTo(date)))
                lastIndexExceedingDate = index;
        }
        int offset = days.size() > 0 ? 1 : 0;
        if (lastIndexExceedingDate > - 1) {
            days.add(lastIndexExceedingDate + offset, date);
            listenerRegistry.forEach(listener -> listener.calendarEventTriggered(DATE_ADDED));
        }
        return this;
    }

    /**
     * @param dayOfWeek
     * @return
     */
    public List<LocalDate> getDatesForDaysOfWeek(final DayOfWeek dayOfWeek) {

        notNull(dayOfWeek, "Mandatory argument 'dayOfWeek' is missing.");
        return days.stream()
                .filter(date -> date.getDayOfWeek().equals(dayOfWeek))
                .collect(Collectors.toList());
    }

    /**
     * @return
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @return
     */
    public LocalDate getStartDate() {
        return endDate.minusDays(calendarPeriod);
    }

    /**
     * @param date
     * @return
     */
    public final boolean isFirstDayInTheMonth(final LocalDate date) {

        notNull(date, MANDATORY_ARGUMENT_DATE_IS_MISSING);
        return days.contains(date) && getDayBefore(date).get().getMonthValue() != date.getMonthValue();
    }

    /**
     * @param date
     * @param offset
     * @return
     */
    public boolean isDayOfTheMonth(final LocalDate date, final int offset) {

        notNull(date, MANDATORY_ARGUMENT_DATE_IS_MISSING);
        state(offset >= 0, "Argument 'offset' must be >= 0");
        return days.contains(date)
                && ((days.indexOf(date) + (offset - 1)) <= days.size())
                && (isFirstDayInTheMonth(days.get((days.indexOf(date) + (offset - 1)))));
    }

    /**
     * @param date
     * @return
     */
    public LocalDate getLastDayOfMonthBefore(final LocalDate date) {

        notNull(date, MANDATORY_ARGUMENT_DATE_IS_MISSING);
        return getLastDayOfMonthBefore(date, 1);
    }

    /**
     * @param date
     * @param monthSubtraction
     * @return
     */
    public LocalDate getLastDayOfMonthBefore(final LocalDate date, final int monthSubtraction) {

        notNull(date, MANDATORY_ARGUMENT_DATE_IS_MISSING);
        state(monthSubtraction >= 0, "Argument 'monthSubtraction' must be >= 0");
        final List<LocalDate> daysInMonthBefore = days.stream().filter(
                day -> (day.getMonthValue() == date.minusMonths(monthSubtraction).getMonthValue()) &&
                        (day.getYear() == date.minusMonths(monthSubtraction).getYear()))
                .collect(Collectors.toList());
        if (daysInMonthBefore.isEmpty())
            throw new IllegalStateException("Calendar spans insufficient days for [" +
                    date.minusMonths(monthSubtraction).format(DateTimeFormatter.ofPattern("yyyy-MM")) + "]");
        return daysInMonthBefore.get(0);
    }

    /**
     * @param listener
     * @return
     */
    public LocalDateCalendar register(final LocalDateCalendarListener listener) {

        notNull(listener, "Mandatory argument 'listener' is missing.");
        listenerRegistry.add(listener);
        return this;
    }
}