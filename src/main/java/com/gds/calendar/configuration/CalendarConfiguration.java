package com.gds.calendar.configuration;

import com.gds.calendar.LocalDateCalendar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 24/08/2017
 */
@Import(ConverterConfiguration.class)
@Configuration
public class CalendarConfiguration {

    @Value("${calendarFileName}")
    private LocalDateCalendar calendar;

    @Bean
    public LocalDateCalendar localDateCalendar() {
        return calendar;
    }
}