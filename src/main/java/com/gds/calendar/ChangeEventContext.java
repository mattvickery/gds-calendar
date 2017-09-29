package com.gds.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 13/09/2017
 */
public interface ChangeEventContext {
    Optional<String> getMessage();
    LocalDateCalendar getCalendar();
    List<LocalDate> getDates();
    CalendarChangeEvent getCalendarChangeEvent();
}
