package com.gds.calendar.examples;

import com.gds.calendar.LocalDateCalendar;
import com.gds.calendar.configuration.CalendarConfiguration;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.LocalDate;

import static com.gds.calendar.configuration.ConverterConfiguration.CALENDAR_DATES_LOCATION_PROPERTY_NAME;
import static com.gds.calendar.configuration.ConverterConfiguration.CALENDAR_PROPERTIES_PROPERTY_NAME;
import static java.lang.System.setProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 12/09/2017
 *
 * A simple example of calendar setup and configuration, all you need to do is to set a directory name for the
 * calendar date files and specify a calendar properties file -
 */
public class ManualSpringContainerStartupTest {

    @Test
    public void container() throws IOException {

        // Setup the system properties, matching -DcalendarDatesLocation=../holidays -Dcalendar=../calendar.properties
        setProperty(CALENDAR_DATES_LOCATION_PROPERTY_NAME, new ClassPathResource("holidays").getURL().getPath());
        setProperty(CALENDAR_PROPERTIES_PROPERTY_NAME,
                new ClassPathResource("holidays/calendar.properties").getURL().getPath());

        // 1. Start the container using the supplied calendar configuration class.
        final AnnotationConfigApplicationContext applicationContext
                = new AnnotationConfigApplicationContext(CalendarConfiguration.class);

        // 2. Access the calendar object through Spring's get bean method, the calendar is ready to use.
        final LocalDateCalendar calendar = applicationContext.getBean(LocalDateCalendar.class);

        assertThat(calendar.getStartDate(), is(LocalDate.of(2018, 12, 18)));
        assertThat(calendar.getAllDates().size(), is(2));
        assertThat(calendar.getEndDate(), is(LocalDate.of(2018, 12, 30)));

    }
}
