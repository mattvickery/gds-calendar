package com.gds.calendar;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 21/08/2017
 *
 * Notification callback functional interface for calendar change event dispatch.
 */
@FunctionalInterface
public interface LocalDateCalendarListener {
     void calendarEventTriggered(final CalendarChangeEvent event);
}