package com.gds.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 08/08/2017
 * <p>
 * A class that is able to manage a collection of LocalDate objects using various set-oriented operations such as add,
 * remove, addAll, removeAll. Also provided are facilities in order that com.gds.com.gds.calendar objects can be queried in a number
 * of useful ways including API query methods and a textual query language.
 * <p>
 * For API query methods, the com.gds.com.gds.calendar is able to report its start and end date and has methods that will provide the
 * first and last day of the month in the com.gds.com.gds.calendar as well as the nth day in the com.gds.com.gds.calendar.
 * <p>
 * The com.gds.com.gds.calendar processes operations on the set of days that are included in the com.gds.com.gds.calendar only, this is an important
 * aspect of com.gds.com.gds.calendar operation. For example, if you ask for the last day of the month before a specific date, if the
 * com.gds.com.gds.calendar contains no days for that month then no dates will be returned.
 * <p>
 * Generally speaking, where possible, the com.gds.com.gds.calendar will not throw an exception but return empty collections or Optional
 * objects that contain no values, null values are not used to indicate any status in this com.gds.com.gds.calendar.
 * <p>
 * The string-based query language may be useful to use as a translation layer between a user interface free-text query
 * and the com.gds.com.gds.calendar object or for use within other API's as a simple lookup mechanism.
 */
public class LocalDateCalendar {

    private static final LocalDateCalendar emptyCalendar = new LocalDateCalendar().removeWeekDays().removeWeekendDays();
    private static final int DEF_CALENDAR_PERIOD = 365;
    private static final String DEF_CALENDAR_NAME = "default";
    private final String calendarName;
    private final List<LocalDate> days = new LinkedList<>();
    private final LocalDate endDate;
    private final int calendarPeriod;
    private final List<LocalDateCalendarListener> listenerRegistry = new ArrayList<>();

    /**
     * Create a com.gds.com.gds.calendar with a default duration, default name and default end date of today.
     */
    public LocalDateCalendar() {
        this(now());
    }

    /**
     * Create a com.gds.com.gds.calendar using the default com.gds.com.gds.calendar period defined by LocalDateCalendar.DEF_CALENDAR_PERIOD. All
     * registered listeners are notified of com.gds.com.gds.calendar updates as a result of this method execution.
     *
     * @param endDate the last date in the com.gds.com.gds.calendar.
     * @throws IllegalArgumentException if the endDate is null.
     */
    public LocalDateCalendar(final LocalDate endDate) {
        this(endDate, DEF_CALENDAR_NAME, DEF_CALENDAR_PERIOD);
    }

    /**
     * Create a com.gds.com.gds.calendar using the specified end date, duration and set of registerable listeners. All registered
     * listeners are notified of com.gds.com.gds.calendar updates as a result of this method execution.
     *
     * @param endDate        the last day of the com.gds.com.gds.calendar.
     * @param calendarName   friendly name of the com.gds.com.gds.calendar.
     * @param calendarPeriod specification of the length (in days) of the com.gds.com.gds.calendar.
     * @param listeners      optional event notification listeners.
     * @throws IllegalArgumentException if endDate or calendarName is null.
     * @throws IllegalStateException    if calendarPeriod is less than or equal to zero.
     */
    public LocalDateCalendar(final LocalDate endDate,
                             final String calendarName,
                             final int calendarPeriod,
                             final LocalDateCalendarListener... listeners) {

        notNull(endDate, "Mandatory argument 'endDate' is missing");
        notNull(calendarName, "Mandatory argument 'calendarName' is missing");
        state(calendarPeriod > 0, "Argument 'calendarPeriod' must be > 0");
        this.endDate = endDate;
        this.calendarPeriod = calendarPeriod;
        this.calendarName = calendarName;
        for (int index = 0; index < calendarPeriod; index++)
            days.add(index, endDate.minusDays(index));
        listenerRegistry.addAll(Arrays.asList(listeners));
        listenerRegistry.forEach(listener -> listener.event(context(CalendarChangeEvent.INITIALISED, "Calendar initialised.", this)));
    }

    /**
     * Get the day in the com.gds.com.gds.calendar before the day supplied. Note that the day before may not be numerically
     * equivalent minus 1 as that date may not be present in the com.gds.com.gds.calendar.
     *
     * @param date the search key used for locating the day before.
     * @return an optional date representing the day before the date supplied, if no day before is found then an
     * optional null will be returned.
     * @throws IllegalArgumentException if the date supplied is null or is located is outside of the com.gds.com.gds.calendar
     *                                  dates range, according to com.gds.com.gds.calendar start and com.gds.com.gds.calendar end dates.
     */
    public Optional<LocalDate> getDayBefore(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");
        if (isOutsideOfCalendarRange(date))
            throw new IllegalArgumentException("Date supplied is outside of calendar range.");
        if (date.isEqual(getStartDate()))
            throw new IllegalArgumentException("Date before will be outside of calendar range.");
        if (days.isEmpty())
            throw new IllegalArgumentException("Cannot use getDayBefore(...) on an empty com.gds.com.gdscalendar.");
        if (days.indexOf(date) > -1)
            return Optional.of(days.get(days.indexOf(date) + 1));
        int nearestAfterIndex = days.size() - 1;
        for (int index = days.size() - 1; index >= 0; index--)
            if (date.isBefore(days.get(index)))
                nearestAfterIndex = index;
        return Optional.of(days.get(nearestAfterIndex));
    }

    /**
     * Locate the supplied date in the com.gds.com.gds.calendar.
     *
     * @param date used as a search key.
     * @return the date located in this com.gds.com.gds.calendar if it exists, an optional null otherwise.
     * @throws IllegalArgumentException if the date argument is null.
     */
    public Optional<LocalDate> getDay(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");
        return days.stream().filter(date::equals).findFirst();
    }

    /**
     * A shortcut method to remove all com.gds.com.gds.calendar dates that are equivalent to a weekend day. All registered listeners
     * are notified of com.gds.com.gds.calendar updates as a result of this method execution.
     *
     * @return the com.gds.com.gds.calendar instance.
     */
    public LocalDateCalendar removeWeekendDays() {
        remove(DayOfWeek.SATURDAY).remove(DayOfWeek.SUNDAY);
        listenerRegistry.forEach(listener -> listener.event(
                context(CalendarChangeEvent.DAY_OF_WEEK_REMOVED, "All weekend dates have been removed from calendar.", this)));
        return this;
    }

    /**
     * A shortcut method to remove all weekdays from the com.gds.com.gds.calendar. All registered listeners are notified of com.gds.com.gds.calendar
     * updates as a result of this method execution.
     *
     * @return the com.gds.com.gds.calendar instance.
     */
    public LocalDateCalendar removeWeekDays() {
        remove(DayOfWeek.MONDAY).remove(DayOfWeek.TUESDAY).remove(DayOfWeek.WEDNESDAY)
                .remove(DayOfWeek.THURSDAY).remove(DayOfWeek.FRIDAY);
        listenerRegistry.forEach(listener -> listener.event(
                context(CalendarChangeEvent.DAY_OF_WEEK_REMOVED, "All weekday dates have been removed from calendar.", this)));
        return this;
    }

    /**
     * Remove the supplied date from the com.gds.com.gds.calendar. If 'ignoreNotLocated' is set to true then non-located date value will
     * be ignored, otherwise an exception will be thrown if the date is not located. All registered listeners are
     * notified of com.gds.com.gds.calendar updates as a result of this method execution.
     *
     * @param date the value to remove from the com.gds.com.gds.calendar.
     * @return the com.gds.com.gds.calendar instance.
     * @throws IllegalArgumentException if the date argument is null and if the 'ignoreNotLocated' is set to false and
     *                                  the date is not located in the com.gds.com.gds.calendar.
     */
    public LocalDateCalendar remove(final LocalDate date, final boolean ignoreNotLocated) {

        notNull(date, "Mandatory argument 'dates' is missing.");
        if ((!ignoreNotLocated) && (!days.contains(date)))
            throw new IllegalArgumentException("Date supplied is not managed by this calendar.");
        if (days.remove(date))
            listenerRegistry.forEach(listener -> listener.event(
                    context(CalendarChangeEvent.DATE_REMOVED, "Date removed from calendar.", this, date)));
        return this;
    }

    /**
     * Remove the date from the com.gds.com.gds.calendar, if the date is not present in the com.gds.com.gds.calendar then the remove operation
     * will return silently without attempting to delete the date.
     *
     * @param date
     * @return
     */
    public LocalDateCalendar remove(final LocalDate date) {
        return remove(date, false);
    }

    /**
     * Remove all dates in the supplied collection from the com.gds.com.gds.calendar. If the collection is empty, an exception will not
     * be thrown, no work will be executed and the method will return silently. If the 'ignoreUnknownDates' flag is set
     * to true then rather than throw an exception, the date will be silently ignored. All registered listeners are
     * notified of com.gds.com.gds.calendar updates as a result of this method execution.
     *
     * @param dates the collection of dates to remove from the com.gds.com.gds.calendar.
     * @return the com.gds.com.gds.calendar instance.
     * @throws IllegalArgumentException if the supplied collection is null.
     * @throws IllegalArgumentException if the ignoreUnknownDates flag is set to false and any of the dates supplied are
     *                                  not managed by this com.gds.com.gds.calendar.
     */
    public LocalDateCalendar removeAll(final List<LocalDate> dates, final boolean ignoreUnknownDates) {

        notNull(dates, "Mandatory argument 'dates' is missing.");
        if ((!ignoreUnknownDates) && (dates.stream().anyMatch(date -> !this.getDay(date).isPresent())))
            throw new IllegalArgumentException("One or more dates supplied is not managed by this calendar.");
        if (days.removeAll(dates))
            listenerRegistry.forEach(listener -> listener.event(
                    context(CalendarChangeEvent.DATES_REMOVED, "Collection of dates removed from calendar.",
                            this, dates.toArray(new LocalDate[]{}))
                    )
            );
        return this;
    }

    /**
     * A shortcut method for com.gds.com.gds.calendar.removeAll(dates, false);
     *
     * @param dates the dates to be removed from the com.gds.com.gds.calendar. If any date in the supplied set is not in the dates
     *              managed by this com.gds.com.gds.calendar, an exception will be thrown.
     * @return the com.gds.com.gds.calendar instance.
     * @throws IllegalArgumentException if the argument is null.
     */
    public LocalDateCalendar removeAll(final List<LocalDate> dates) {
        return removeAll(dates, false);
    }

    /**
     * Instead of using a date value to remove a date from the com.gds.com.gds.calendar, you can remove a set of dates that have a
     * specific day-of-week value. All com.gds.com.gds.calendar dates that have a matching day-of-week value will be removed from
     * the com.gds.com.gds.calendar. All registered listeners are notified of com.gds.com.gds.calendar updates as a result of this method execution.
     *
     * @param dayOfWeek
     * @return the com.gds.com.gds.calendar instance.
     * @throws IllegalArgumentException if the argument is null.
     */
    public LocalDateCalendar remove(final DayOfWeek dayOfWeek) {

        notNull(dayOfWeek, "Mandatory argument 'dayOfWeek' is missing.");
        final List<LocalDate> matchingDayOfWeeks = days.stream()
                .filter(day -> day.getDayOfWeek().equals(dayOfWeek))
                .collect(Collectors.toList());
        if (days.removeAll(matchingDayOfWeeks))
            listenerRegistry.forEach(listener -> listener.event(
                    context(CalendarChangeEvent.DAY_OF_WEEK_REMOVED, "Day of Week removed [" + dayOfWeek + "]", this)));
        return this;
    }

    /**
     * Remove all dates in the supplied com.gds.com.gds.calendar from this com.gds.com.gds.calendar. All registered listeners are notified of com.gds.com.gds.calendar
     * updates as a result of this method execution.
     *
     * @param calendar the com.gds.com.gds.calendar supplying a list of dates to remove from this com.gds.com.gds.calendar.
     * @return the com.gds.com.gds.calendar instance.
     * @throws IllegalArgumentException if the com.gds.com.gds.calendar argument is null.
     */
    public LocalDateCalendar remove(final LocalDateCalendar calendar) {

        notNull(calendar, "Mandatory argument 'calendar' is missing.");
        state(this != calendar, "A calendar cannot be removed from itself.");
        calendar.days.forEach(date -> this.remove(date, true));
        listenerRegistry.forEach(listener -> listener.event(
                context(CalendarChangeEvent.CALENDAR_REMOVED, "Calendar dates from " + calendar.getName() + " removed from "
                        + getName() + ".", calendar)));
        return this;
    }

    /**
     * Add all dates located in the supplied com.gds.com.gds.calendar to this com.gds.com.gds.calendar. Exceptions will not be thrown if the com.gds.com.gds.calendar
     * is empty. All registered listeners are notified of com.gds.com.gds.calendar updates as a result of this method execution.
     *
     * @param calendar
     * @return the com.gds.com.gds.calendar instance.
     * @throws IllegalArgumentException if the com.gds.com.gds.calendar argument is null.
     */
    public LocalDateCalendar add(final LocalDateCalendar calendar) {

        notNull(calendar, "Mandatory argument 'calendar' is missing.");
        calendar.days.forEach(this::add);
        listenerRegistry.forEach(listener -> listener.event(
                context(CalendarChangeEvent.CALENDAR_ADDED, "Calendar dates from " + calendar.getName() + " added to "
                        + getName() + ".", calendar)));
        return this;
    }

    /**
     * Add a new date to the com.gds.com.gds.calendar. Although this is a simple method, there are restrictions on entry. The date
     * supplied has to be after the com.gds.com.gds.calendar start date and before the com.gds.com.gds.calendar end date. If the date already exists
     * in the com.gds.com.gds.calendar it will not be added and no exception will be thrown. All registered listeners are notified of
     * com.gds.com.gds.calendar updates as a result of this method execution.
     * <p>
     * Note:- If successfully inserted, a date will be inserted between a pair of dates. Those dates may not be plus
     * and minus 1 day of the date supplied as it's quite possible to have gaps in the com.gds.com.gds.calendar.
     *
     * @param date the date which will undergo attempted insertion.
     * @return the com.gds.com.gds.calendar instance.
     * @throws IllegalArgumentException if the supplied date is null or outside the com.gds.com.gds.calendar range.
     */
    public LocalDateCalendar add(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");

        if (isOutsideOfCalendarRange(date))
            throw new IllegalArgumentException("Date supplied is outside of calendar range.");
        if (days.indexOf(date) > -1)
            return this;
        int lastIndexExceedingDate = 0;
        for (int index = 0; index < days.size(); index++)
            if (days.get(index).isBefore(date))
                lastIndexExceedingDate = index;
        int offset = days.size() > 0 ? 1 : 0;
        if (lastIndexExceedingDate > -1) {
            days.add(lastIndexExceedingDate + offset, date);
            listenerRegistry.forEach(listener -> listener.event(context(CalendarChangeEvent.DATE_ADDED,
                    "New date added to calendar.", this, date)));
        }
        return this;
    }

    /**
     * Return all of the dates that correspond to the specified day-of-the week argument. The list returned may
     * contain zero, one or more elements.
     *
     * @param dayOfWeek the lookup key.
     * @return the com.gds.com.gds.calendar instance.
     * @throws IllegalArgumentException if the dayOfWeek argument is null.
     */
    public List<LocalDate> getDatesForDaysOfWeek(final DayOfWeek dayOfWeek) {

        notNull(dayOfWeek, "Mandatory argument 'dayOfWeek' is missing.");
        return days.stream()
                .filter(date -> date.getDayOfWeek().equals(dayOfWeek))
                .collect(Collectors.toList());
    }

    /**
     * Return the last possible date found in this com.gds.com.gds.calendar. As dates can be added and removed, there is no guarantee
     * that the com.gds.com.gds.calendar actually contains a date representing the end date.
     *
     * @return the end date for this com.gds.com.gds.calendar.
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Return the start date configured for this com.gds.com.gds.calendar. This method calculates the start date and returns it
     * irrespective of the presence of the start date in the com.gds.com.gds.calendar. For example, although the method
     * com.gds.com.gds.calendar.getStartDate() may return a value the method com.gds.com.gds.calendar.getDay(com.gds.com.gds.calendar.getStartDate) may not return a
     * value.
     *
     * @return the start date for this com.gds.com.gds.calendar.
     */
    public LocalDate getStartDate() {
        return endDate.minusDays(calendarPeriod - 1);
    }

    /**
     * Return a representation that this is the first day in the month. This query works on a com.gds.com.gds.calendar object that
     * may have had days removed so the first day in the month may be specific to each com.gds.com.gds.calendar instance and to each
     * month's days state.
     *
     * @param date the lookup key.
     * @return true if the date supplied is the first date in the month, false otherwise.
     * @throws IllegalArgumentException if the supplied date is null or outside the com.gds.com.gds.calendar range.
     */
    public final boolean isFirstDayInTheMonth(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");
        if (isOutsideOfCalendarRange(date))
            throw new IllegalArgumentException("Date supplied is outside of calendar range.");
        return days.contains(date) && getDayBefore(date).get().getMonthValue() != date.getMonthValue();
    }

    /**
     * A com.gds.com.gds.calendar can span multiple years and can contain the same month value across more than one year. For the year
     * and month value supplied, return the first day in the com.gds.com.gds.calendar. If the year and month are not found then an
     * empty Optional type will be returned.
     *
     * @param year  the year filter.
     * @param month the month filter.
     * @return an Optional date representing the first day of the located month or empty value.
     * @throws IllegalArgumentException if the supplied year is null.
     * @throws IllegalArgumentException if the supplied month is null.
     */
    public Optional<LocalDate> getFirstDayOfTheMonth(final Year year, final Month month) {

        notNull(year, "Mandatory argument 'year' is missing.");
        notNull(month, "Mandatory argument 'month' is missing.");

        final List<LocalDate> subsetOfDays = getDaysInMonth(year, month);
        if (subsetOfDays.isEmpty())
            return Optional.empty();
        return Optional.of(subsetOfDays.get(subsetOfDays.size() - 1));
    }

    /**
     * Get all dates in the year and month specified. The list of dates may contain zero, one or many elements.
     *
     * @param year  the year filter.
     * @param month the month filter.
     * @return a list of dates that are managed by the com.gds.com.gds.calendar, filtered by year and month.
     * @throws IllegalArgumentException if the supplied year is null.
     * @throws IllegalArgumentException if the supplied month is null.
     */
    public List<LocalDate> getDaysInMonth(final Year year, final Month month) {

        notNull(year, "Mandatory argument 'year' is missing.");
        notNull(month, "Mandatory argument 'month' is missing.");
        return getDaysInYear(year).stream().filter(
                date -> month.getValue() == date.getMonthValue()).collect(Collectors.toList());
    }

    /**
     * Get all dates in the month specified irrespective of the year. The list of dates may contain zero, one or
     * many elements.
     *
     * @param month the month filter.
     * @return a list of dates that are managed by the com.gds.com.gds.calendar, filtered by month.
     * @throws IllegalArgumentException if the supplied month is null.
     */
    public List<LocalDate> getDaysInMonth(final Month month) {

        notNull(month, "Mandatory argument 'month' is missing.");
        return getAllDates().stream().filter(
                date -> month.getValue() == date.getMonthValue()).collect(Collectors.toList());
    }

    /**
     * Get all dates in the month specified. The list of dates may contain zero, one or many elements.
     *
     * @param year the year filter.
     * @return a list of dates that are managed by the com.gds.com.gds.calendar, filtered by year.
     * @throws IllegalArgumentException if the supplied year is null.
     */
    public List<LocalDate> getDaysInYear(final Year year) {

        notNull(year, "Mandatory argument 'year' is missing.");
        return getAllDates().stream().filter(date -> date.getYear() == year.getValue()).collect(Collectors.toList());
    }

    /**
     * A query method that allows the caller to determine if the date supplied is the 'dayOffset' day in the month of the
     * month and year of the date supplied. For example: isDayOfTheMonth(LocalDate.of(2018,01,01), 1) will yield true if
     * the com.gds.com.gds.calendar is filled with all dates for the year 2018. isDayOfTheMonth(LocalDate.of(2018,01,01), 2) will always
     * yield false. isDayOfTheMonth(LocalDate.of(2018,01,05), 2) may yield true or false depending on com.gds.com.gds.calendar contents.
     *
     * @param date      the value used as a lookup key in the set of managed dates.
     * @param dayOffset the dayOffset in days used for comparison purposes.
     * @return true if the date supplied is the correct dayOffset in the month, false otherwise.
     * @throws IllegalArgumentException if the supplied date is null or outside the com.gds.com.gds.calendar range.
     * @throws IllegalStateException    if the supplied dayOffset is not >= zero.
     */
    public boolean isDayOfTheMonth(final LocalDate date, final int dayOffset) {

        notNull(date, "Mandatory argument 'date' is missing.");
        if (isOutsideOfCalendarRange(date))
            throw new IllegalArgumentException("Date supplied is outside of calendar range.");
        state(dayOffset >= 0, "Argument 'dayOffset' must be >= 0");
        return days.contains(date)
                && ((days.indexOf(date) + (dayOffset - 1)) <= days.size())
                && (isFirstDayInTheMonth(days.get((days.indexOf(date) + (dayOffset - 1)))));
    }

    /**
     * @param date
     * @return
     * @throws IllegalArgumentException if the supplied date is null or outside the com.gds.com.gds.calendar range.
     */
    public Optional<LocalDate> getLastDayOfMonthBefore(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");
        if (isOutsideOfCalendarRange(date))
            throw new IllegalArgumentException("Date supplied is outside of calendar range.");
        return getLastDayOfMonthBefore(date, 1);
    }

    /**
     * @param date
     * @param monthSubtraction
     * @return
     * @throws IllegalArgumentException iif the supplied date is null or outside the com.gds.com.gds.calendar range.
     * @throws IllegalStateException    if the supplied monthSubtraction is not >= zero.
     */
    public Optional<LocalDate> getLastDayOfMonthBefore(final LocalDate date, final int monthSubtraction) {

        notNull(date, "Mandatory argument 'date' is missing.");
        state(monthSubtraction >= 0, "Argument 'monthSubtraction' must be >= 0");
        if (isOutsideOfCalendarRange(date))
            throw new IllegalArgumentException("Date supplied is outside of calendar range.");
        final List<LocalDate> daysInMonthBefore = days.stream().filter(
                day -> (day.getMonthValue() == date.minusMonths(monthSubtraction).getMonthValue()) &&
                        (day.getYear() == date.minusMonths(monthSubtraction).getYear()))
                .collect(Collectors.toList());
        return Optional.ofNullable(!daysInMonthBefore.isEmpty() ? daysInMonthBefore.get(0) : null);
    }

    /**
     * Register an listener with this com.gds.com.gds.calendar, single event notification granularity only is possible, listeners
     * either register for all or for no events.
     *
     * @param listener an implementation of the listener interface.
     * @return the com.gds.com.gds.calendar instance.
     * @throws IllegalArgumentException if the supplied listener is null.
     */
    public LocalDateCalendar register(final LocalDateCalendarListener listener) {

        notNull(listener, "Mandatory argument 'listener' is missing.");
        listenerRegistry.add(listener);
        return this;
    }

    /**
     * Return all dates supported by this com.gds.com.gds.calendar. The days list is owned and managed by this com.gds.com.gds.calendar and so an
     * unmodifiable list is returned, any externally made changes will not be reflected inside this com.gds.com.gds.calendar.
     *
     * @return an unmodifiable list of dates that are managed by this com.gds.com.gds.calendar.
     */
    public List<LocalDate> getAllDates() {
        return Collections.unmodifiableList(days);
    }

    /**
     * The friendly name of this com.gds.com.gds.calendar instance.
     *
     * @return a textual name of this com.gds.com.gds.calendar.
     */
    public String getName() {
        return calendarName;
    }

    /**
     * Single instance of an empty com.gds.com.gds.calendar.
     *
     * @return a default com.gds.com.gds.calendar with weekdays and weekends removed.
     */
    public static LocalDateCalendar empty() {
        return emptyCalendar;
    }

    /**
     * Is the supplied date within the range of dates that this com.gds.com.gds.calendar handles.
     * @param date the date with which to do the lookup.
     * @return true if the date is outside the range handled by this com.gds.com.gds.calendar, false otherwise.
     */
    public boolean isOutsideOfCalendarRange(final LocalDate date) {
        return date.isAfter(endDate) || date.isBefore(getStartDate());
    }

    /**
     * A shortcut for creating an event context object.
     *
     * @param message  an optional message, use null if not required, protected by Optional.
     * @param calendar a mandatory com.gds.com.gds.calendar object.
     * @param dates    an optional list of dates.
     * @return
     */
    private ChangeEventContext context(final CalendarChangeEvent event, final String message,
                                       final LocalDateCalendar calendar, final LocalDate... dates) {

        notNull(calendar, "Mandatory argument 'calendar' is missing.");
        return new ChangeEventContext() {
            @Override
            public Optional<String> getMessage() {
                return Optional.ofNullable(message);
            }

            @Override
            public LocalDateCalendar getCalendar() {
                return calendar;
            }

            @Override
            public List<LocalDate> getDates() {
                return Arrays.asList(dates);
            }

            @Override
            public CalendarChangeEvent getCalendarChangeEvent() {
                return event;
            }
        };
    }
}