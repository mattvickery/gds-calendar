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

import static com.gds.calendar.CalendarChangeEvent.CALENDAR_ADDED;
import static com.gds.calendar.CalendarChangeEvent.CALENDAR_REMOVED;
import static com.gds.calendar.CalendarChangeEvent.DATES_REMOVED;
import static com.gds.calendar.CalendarChangeEvent.DATE_ADDED;
import static com.gds.calendar.CalendarChangeEvent.DATE_REMOVED;
import static com.gds.calendar.CalendarChangeEvent.DAY_OF_WEEK_REMOVED;
import static com.gds.calendar.CalendarChangeEvent.INITIALISED;
import static java.time.LocalDate.now;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 08/08/2017
 * <p>
 * A class that is able to manage a collection of LocalDate objects using various set-oriented operations such as add,
 * remove, addAll, removeAll. Also provided are facilities in order that calendar objects can be queried in a number
 * of useful ways including API query methods and a textual query language.
 * <p>
 * For API query methods, the calendar is able to report its start and end date and has methods that will provide the
 * first and last day of the month in the calendar as well as the nth day in the calendar.
 * <p>
 * The calendar processes operations on the set of days that are included in the calendar only, this is an important
 * aspect of calendar operation. For example, if you ask for the last day of the month before a specific date, if the
 * calendar contains no days for that month then no dates will be returned.
 * <p>
 * Generally speaking, where possible, the calendar will not throw an exception but return empty collections or Optional
 * objects that contain no values, null values are not used to indicate any status in this calendar.
 * <p>
 * The string-based query language may be useful to use as a translation layer between a user interface free-text query
 * and the calendar object or for use within other API's as a simple lookup mechanism.
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
     * Create a calendar with a default duration, default name and default end date of today.
     */
    public LocalDateCalendar() {
        this(now());
    }

    /**
     * Create a calendar using the default calendar period defined by LocalDateCalendar.DEF_CALENDAR_PERIOD. All
     * registered listeners are notified of calendar updates as a result of this method execution.
     *
     * @param endDate the last date in the calendar.
     * @throws IllegalArgumentException if the endDate is null.
     */
    public LocalDateCalendar(final LocalDate endDate) {
        this(endDate, DEF_CALENDAR_NAME, DEF_CALENDAR_PERIOD);
    }

    /**
     * Create a calendar using the specified end date, duration and set of registerable listeners. All registered
     * listeners are notified of calendar updates as a result of this method execution.
     *
     * @param endDate        the last day of the calendar.
     * @param calendarName   friendly name of the calendar.
     * @param calendarPeriod specification of the length (in days) of the calendar.
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
        listenerRegistry.forEach(listener -> listener.event(context(INITIALISED, "Calendar initialised.", this)));
    }

    /**
     * Get the day in the calendar before the day supplied. Note that the day before may not be numerically
     * equivalent minus 1 as that date may not be present in the calendar.
     *
     * @param date the search key used for locating the day before.
     * @return an optional date representing the day before the date supplied, if no day before is found then an
     * optional null will be returned.
     * @throws IllegalArgumentException if the date supplied is null or is located is outside of the calendar
     *                                  dates range, according to calendar start and calendar end dates.
     */
    public Optional<LocalDate> getDayBefore(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");
        if ((date.isAfter(getEndDate())) || (date.isBefore(getStartDate())))
            throw new IllegalArgumentException("Date supplied is outside of calendar range.");
        if (date.isEqual(getStartDate()))
            throw new IllegalArgumentException("Date before will be outside of calendar range.");
        if (days.isEmpty())
            throw new IllegalArgumentException("Cannot use getDayBefore(...) on an empty calendar.");
        if (days.indexOf(date) > -1)
            return Optional.of(days.get(days.indexOf(date) + 1));
        int nearestAfterIndex = days.size() - 1;
        for (int index = days.size() - 1; index >= 0; index--)
            if (date.isBefore(days.get(index)))
                nearestAfterIndex = index;
        return Optional.of(days.get(nearestAfterIndex));
    }

    /**
     * Locate the supplied date in the calendar.
     *
     * @param date used as a search key.
     * @return the date located in this calendar if it exists, an optional null otherwise.
     * @throws IllegalArgumentException if the date argument is null.
     */
    public Optional<LocalDate> getDay(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");
        return days.stream().filter(date::equals).findFirst();
    }

    /**
     * A shortcut method to remove all calendar dates that are equivalent to a weekend day. All registered listeners
     * are notified of calendar updates as a result of this method execution.
     *
     * @return the calendar instance.
     */
    public LocalDateCalendar removeWeekendDays() {
        remove(DayOfWeek.SATURDAY).remove(DayOfWeek.SUNDAY);
        listenerRegistry.forEach(listener -> listener.event(
                context(DAY_OF_WEEK_REMOVED, "All weekend dates have been removed from calendar.", this)));
        return this;
    }

    /**
     * A shortcut method to remove all weekdays from the calendar. All registered listeners are notified of calendar
     * updates as a result of this method execution.
     *
     * @return the calendar instance.
     */
    public LocalDateCalendar removeWeekDays() {
        remove(DayOfWeek.MONDAY).remove(DayOfWeek.TUESDAY).remove(DayOfWeek.WEDNESDAY)
                .remove(DayOfWeek.THURSDAY).remove(DayOfWeek.FRIDAY);
        listenerRegistry.forEach(listener -> listener.event(
                context(DAY_OF_WEEK_REMOVED, "All weekday dates have been removed from calendar.", this)));
        return this;
    }

    /**
     * Remove the supplied date from the calendar. If 'ignoreNotLocated' is set to true then non-located date value will
     * be ignored, otherwise an exception will be thrown if the date is not located. All registered listeners are
     * notified of calendar updates as a result of this method execution.
     *
     * @param date the value to remove from the calendar.
     * @return the calendar instance.
     * @throws IllegalArgumentException if the date argument is null and if the 'ignoreNotLocated' is set to false and
     *                                  the date is not located in the calendar.
     */
    public LocalDateCalendar remove(final LocalDate date, final boolean ignoreNotLocated) {

        notNull(date, "Mandatory argument 'dates' is missing.");
        if ((!ignoreNotLocated) && (!days.contains(date)))
            throw new IllegalArgumentException("Date supplied is not managed by this calendar.");
        if (days.remove(date))
            listenerRegistry.forEach(listener -> listener.event(
                    context(DATE_REMOVED, "Date removed from calendar.", this, date)));
        return this;
    }

    /**
     * Remove the date from the calendar, if the date is not present in the calendar then the remove operation
     * will return silently without attempting to delete the date.
     *
     * @param date
     * @return
     */
    public LocalDateCalendar remove(final LocalDate date) {
        return remove(date, false);
    }

    /**
     * Remove all dates in the supplied collection from the calendar. If the collection is empty, an exception will not
     * be thrown, no work will be executed and the method will return silently. If the 'ignoreUnknownDates' flag is set
     * to true then rather than throw an exception, the date will be silently ignored. All registered listeners are
     * notified of calendar updates as a result of this method execution.
     *
     * @param dates the collection of dates to remove from the calendar.
     * @return the calendar instance.
     * @throws IllegalArgumentException if the supplied collection is null.
     * @throws IllegalArgumentException if the ignoreUnknownDates flag is set to false and any of the dates supplied are
     *                                  not managed by this calendar.
     */
    public LocalDateCalendar removeAll(final List<LocalDate> dates, final boolean ignoreUnknownDates) {

        notNull(dates, "Mandatory argument 'dates' is missing.");
        if ((!ignoreUnknownDates) && (dates.stream().anyMatch(date -> !this.getDay(date).isPresent())))
            throw new IllegalArgumentException("One or more dates supplied is not managed by this calendar.");
        if (days.removeAll(dates))
            listenerRegistry.forEach(listener -> listener.event(
                    context(DATES_REMOVED, "Collection of dates removed from calendar.",
                            this, dates.toArray(new LocalDate[]{}))
                    )
            );
        return this;
    }

    /**
     * A shortcut method for calendar.removeAll(dates, false);
     *
     * @param dates the dates to be removed from the calendar. If any date in the supplied set is not in the dates
     *              managed by this calendar, an exception will be thrown.
     * @return the calendar instance.
     * @throws IllegalArgumentException if the argument is null.
     */
    public LocalDateCalendar removeAll(final List<LocalDate> dates) {
        return removeAll(dates, false);
    }

    /**
     * Instead of using a date value to remove a date from the calendar, you can remove a set of dates that have a
     * specific day-of-week value. All calendar dates that have a matching day-of-week value will be removed from
     * the calendar. All registered listeners are notified of calendar updates as a result of this method execution.
     *
     * @param dayOfWeek
     * @return the calendar instance.
     * @throws IllegalArgumentException if the argument is null.
     */
    public LocalDateCalendar remove(final DayOfWeek dayOfWeek) {

        notNull(dayOfWeek, "Mandatory argument 'dayOfWeek' is missing.");
        final List<LocalDate> matchingDayOfWeeks = days.stream()
                .filter(day -> day.getDayOfWeek().equals(dayOfWeek))
                .collect(Collectors.toList());
        if (days.removeAll(matchingDayOfWeeks))
            listenerRegistry.forEach(listener -> listener.event(
                    context(DAY_OF_WEEK_REMOVED, "Day of Week removed [" + dayOfWeek + "]", this)));
        return this;
    }

    /**
     * Remove all dates in the supplied calendar from this calendar. All registered listeners are notified of calendar
     * updates as a result of this method execution.
     *
     * @param calendar the calendar supplying a list of dates to remove from this calendar.
     * @return the calendar instance.
     * @throws IllegalArgumentException if the calendar argument is null.
     */
    public LocalDateCalendar remove(final LocalDateCalendar calendar) {

        notNull(calendar, "Mandatory argument 'calendar' is missing.");
        state(this != calendar, "A calendar cannot be removed from itself.");
        calendar.days.forEach(date -> this.remove(date, true));
        listenerRegistry.forEach(listener -> listener.event(
                context(CALENDAR_REMOVED, "Calendar dates from " + calendar.getName() + " removed from "
                        + getName() + ".", calendar)));
        return this;
    }

    /**
     * Add all dates located in the supplied calendar to this calendar. Exceptions will not be thrown if the calendar
     * is empty. All registered listeners are notified of calendar updates as a result of this method execution.
     *
     * @param calendar
     * @return the calendar instance.
     * @throws IllegalArgumentException if the calendar argument is null.
     */
    public LocalDateCalendar add(final LocalDateCalendar calendar) {

        notNull(calendar, "Mandatory argument 'calendar' is missing.");
        calendar.days.forEach(this::add);
        listenerRegistry.forEach(listener -> listener.event(
                context(CALENDAR_ADDED, "Calendar dates from " + calendar.getName() + " added to "
                        + getName() + ".", calendar)));
        return this;
    }

    /**
     * Add a new date to the calendar. Although this is a simple method, there are restrictions on entry. The date
     * supplied has to be after the calendar start date and before the calendar end date. If the date already exists
     * in the calendar it will not be added and no exception will be thrown. All registered listeners are notified of
     * calendar updates as a result of this method execution.
     * <p>
     * Note:- If successfully inserted, a date will be inserted between a pair of dates. Those dates may not be plus
     * and minus 1 day of the date supplied as it's quite possible to have gaps in the calendar.
     *
     * @param date the date which will undergo attempted insertion.
     * @return the calendar instance.
     * @throws IllegalArgumentException if the supplied date is null.
     */
    public LocalDateCalendar add(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");

        if (date.isAfter(endDate) || date.isBefore(getStartDate()))
            throw new IllegalStateException("Date supplied is outside of calendar range.");
        if (days.indexOf(date) > -1)
            return this;
        int lastIndexExceedingDate = 0;
        for (int index = 0; index < days.size(); index++)
            if (days.get(index).isBefore(date))
                lastIndexExceedingDate = index;
        int offset = days.size() > 0 ? 1 : 0;
        if (lastIndexExceedingDate > -1) {
            days.add(lastIndexExceedingDate + offset, date);
            listenerRegistry.forEach(listener -> listener.event(context(DATE_ADDED,
                    "New date added to calendar.", this, date)));
        }
        return this;
    }

    /**
     * Return all of the dates that correspond to the specified day-of-the week argument. The list returned may
     * contain zero, one or more elements.
     *
     * @param dayOfWeek the lookup key.
     * @return the calendar instance.
     * @throws IllegalArgumentException if the dayOfWeek argument is null.
     */
    public List<LocalDate> getDatesForDaysOfWeek(final DayOfWeek dayOfWeek) {

        notNull(dayOfWeek, "Mandatory argument 'dayOfWeek' is missing.");
        return days.stream()
                .filter(date -> date.getDayOfWeek().equals(dayOfWeek))
                .collect(Collectors.toList());
    }

    /**
     * Return the last possible date found in this calendar. As dates can be added and removed, there is no guarantee
     * that the calendar actually contains a date representing the end date.
     *
     * @return the end date for this calendar.
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Return the start date configured for this calendar. This method calculates the start date and returns it
     * irrespective of the presence of the start date in the calendar. For example, although the method
     * calendar.getStartDate() may return a value the method calendar.getDay(calendar.getStartDate) may not return a
     * value.
     *
     * @return the start date for this calendar.
     */
    public LocalDate getStartDate() {
        return endDate.minusDays(calendarPeriod - 1);
    }

    /**
     * Return a representation that this is the first day in the month. This query works on a calendar object that
     * may have had days removed so the first day in the month may be specific to each calendar instance and to each
     * month's days state.
     *
     * @param date the lookup key.
     * @return true if the date supplied is the first date in the month, false otherwise.
     * @throws IllegalArgumentException if the supplied date is null.
     */
    public final boolean isFirstDayInTheMonth(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");
        return days.contains(date) && getDayBefore(date).get().getMonthValue() != date.getMonthValue();
    }

    /**
     * A calendar can span multiple years and can contain the same month value across more than one year. For the year
     * and month value supplied, return the first day in the calendar. If the year and month are not found then an
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
            Optional.empty();
        return Optional.of(subsetOfDays.get(0));
    }

    /**
     * Get all dates in the year and month specified. The list of dates may contain zero, one or many elements.
     *
     * @param year  the year filter.
     * @param month the month filter.
     * @return a list of dates that are managed by the calendar, filtered by year and month.
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
     * @return a list of dates that are managed by the calendar, filtered by month.
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
     * @return a list of dates that are managed by the calendar, filtered by year.
     * @throws IllegalArgumentException if the supplied year is null.
     */
    public List<LocalDate> getDaysInYear(final Year year) {

        notNull(year, "Mandatory argument 'year' is missing.");
        return getAllDates().stream().filter(date -> date.getYear() == year.getValue()).collect(Collectors.toList());
    }

    /**
     * A query method that allows the caller to determine if the date supplied is the 'dayOffset' day in the month of the
     * month and year of the date supplied. For example: isDayOfTheMonth(LocalDate.of(2018,01,01), 1) will yield true if
     * the calendar is filled with all dates for the year 2018. isDayOfTheMonth(LocalDate.of(2018,01,01), 2) will always
     * yield false. isDayOfTheMonth(LocalDate.of(2018,01,05), 2) may yield true or false depending on calendar contents.
     *
     * @param date      the value used as a lookup key in the set of managed dates.
     * @param dayOffset the dayOffset in days used for comparison purposes.
     * @return true if the date supplied is the correct dayOffset in the month, false otherwise.
     * @throws IllegalArgumentException if the supplied date is null.
     * @throws IllegalStateException    if the supplied dayOffset is not >= zero.
     */
    public boolean isDayOfTheMonth(final LocalDate date, final int dayOffset) {

        notNull(date, "Mandatory argument 'date' is missing.");
        state(dayOffset >= 0, "Argument 'dayOffset' must be >= 0");
        return days.contains(date)
                && ((days.indexOf(date) + (dayOffset - 1)) <= days.size())
                && (isFirstDayInTheMonth(days.get((days.indexOf(date) + (dayOffset - 1)))));
    }

    /**
     * @param date
     * @return
     * @throws IllegalArgumentException if the supplied date is null.
     */
    public Optional<LocalDate> getLastDayOfMonthBefore(final LocalDate date) {

        notNull(date, "Mandatory argument 'date' is missing.");
        return getLastDayOfMonthBefore(date, 1);
    }

    /**
     * @param date
     * @param monthSubtraction
     * @return
     * @throws IllegalArgumentException if the supplied date is null.
     * @throws IllegalStateException    if the supplied monthSubtraction is not >= zero.
     */
    public Optional<LocalDate> getLastDayOfMonthBefore(final LocalDate date, final int monthSubtraction) {

        notNull(date, "Mandatory argument 'date' is missing.");
        state(monthSubtraction >= 0, "Argument 'monthSubtraction' must be >= 0");
        final List<LocalDate> daysInMonthBefore = days.stream().filter(
                day -> (day.getMonthValue() == date.minusMonths(monthSubtraction).getMonthValue()) &&
                        (day.getYear() == date.minusMonths(monthSubtraction).getYear()))
                .collect(Collectors.toList());
        return Optional.ofNullable(!daysInMonthBefore.isEmpty() ? daysInMonthBefore.get(0) : null);
    }

    /**
     * Register an listener with this calendar, single event notification granularity only is possible, listeners
     * either register for all or for no events.
     *
     * @param listener an implementation of the listener interface.
     * @return the calendar instance.
     * @throws IllegalArgumentException if the supplied listener is null.
     */
    public LocalDateCalendar register(final LocalDateCalendarListener listener) {

        notNull(listener, "Mandatory argument 'listener' is missing.");
        listenerRegistry.add(listener);
        return this;
    }

    /**
     * Return all dates supported by this calendar. The days list is owned and managed by this calendar and so an
     * unmodifiable list is returned, any externally made changes will not be reflected inside this calendar.
     *
     * @return an unmodifiable list of dates that are managed by this calendar.
     */
    public List<LocalDate> getAllDates() {
        return Collections.unmodifiableList(days);
    }

    /**
     * The friendly name of this calendar instance.
     *
     * @return a textual name of this calendar.
     */
    public String getName() {
        return calendarName;
    }

    /**
     * Create a new calendar from the intersection of the supplied calendars. The new calendar will be configured with
     * a start date that is the earliest in the supplied calendars and with an end date that is the latest in the
     * supplied calendars. All registered listeners will not be transfered from the source calendars to the new
     * target calendar.
     *
     * @param calendars a source of members for the intersection operation.
     * @return a new calendar representing the intersection
     * @throws IllegalArgumentException if the calendars argument is null.
     * @throws IllegalStateException    if the list of calendars is empty.
     */
    public static LocalDateCalendar intersect(final LocalDateCalendar... calendars) {

        notNull(calendars, "Mandatory argument 'calendars' is missing.");
        state(calendars.length < 1, "Cannot intersect on an empty list of calendars.");
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Create a new calendar from the union of supplied calendars. The new calendar will be configured with a start date
     * that is the earliest in the supplied calendars and with an end date that is the latest in the supplied calendars.
     * All registered listeners will not be transfered from the source calendars to the new target calendar.
     *
     * @param calendars a source of members for the union operation.
     * @return a new calendar representing the union of supplied calendars.
     * @throws IllegalArgumentException if the calendars argument is null.
     * @throws IllegalStateException    if the list of calendars is empty.
     */
    public static LocalDateCalendar union(final LocalDateCalendar... calendars) {

        notNull(calendars, "Mandatory argument 'calendars' is missing.");
        state(calendars.length < 1, "Cannot intersect on an empty list of calendars.");
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Single instance of an empty calendar.
     *
     * @return a default calendar with weekdays and weekends removed.
     */
    public static LocalDateCalendar empty() {
        return emptyCalendar;
    }

    /**
     * A shortcut for creating an event context object.
     *
     * @param message  an optional message, use null if not required, protected by Optional.
     * @param calendar a mandatory calendar object.
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