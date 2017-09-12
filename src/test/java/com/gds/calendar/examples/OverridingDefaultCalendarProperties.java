package com.gds.calendar.examples;

import com.gds.calendar.LocalDateCalendar;
import com.gds.calendar.configuration.CalendarConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;

import static com.gds.calendar.configuration.ConverterConfiguration.CALENDAR_DATES_LOCATION_PROPERTY_NAME;
import static com.gds.calendar.configuration.ConverterConfiguration.CALENDAR_PROPERTIES_PROPERTY_NAME;
import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 25/08/2017
 *
 * An example showing an override of property setup. In ordinary use, you can set two Java properties on the
 * command line in order to specify the location and name of the calendar properties file and the name of the
 * directory containing the calendar dates. For example:
 *
 * java -DcalendarDatesLocation=/tmp -D/tmp/calendar.properties
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CalendarConfiguration.class})
@DirtiesContext
public class OverridingDefaultCalendarProperties {

    @Autowired
    private LocalDateCalendar calendar;

    @BeforeClass
    public static void beforeClass() throws Exception {
        setProperty(CALENDAR_DATES_LOCATION_PROPERTY_NAME, new ClassPathResource("override-test").getURL().getPath());
        setProperty(CALENDAR_PROPERTIES_PROPERTY_NAME,
                new ClassPathResource("override-test/calendar.properties").getURL().getPath());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        clearProperty(CALENDAR_DATES_LOCATION_PROPERTY_NAME);
        clearProperty(CALENDAR_PROPERTIES_PROPERTY_NAME);
    }

    @Test
    public void startDate() {
        assertThat(calendar.getStartDate(), is(LocalDate.of(2018,07,19)));
    }

    @Test
    public void duration() {
        assertThat(calendar.getAllDates().size(), is(2));
    }

    @Test
    public void endDate() {
        assertThat(calendar.getEndDate(), is(LocalDate.of(2018,07,31)));
    }
}