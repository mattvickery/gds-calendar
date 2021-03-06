package com.gds.calendar;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 21/08/2017
 *
 * Notification callback functional interface for com.gds.com.gds.calendar change event dispatch.
 */
@FunctionalInterface
public interface LocalDateCalendarListener {
     void event(final ChangeEventContext context);
}