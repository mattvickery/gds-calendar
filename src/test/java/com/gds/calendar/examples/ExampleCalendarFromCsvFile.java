package com.gds.calendar.examples;

import com.gds.calendar.LocalDateCalendar;
import com.gds.calendar.configuration.CalendarConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 24/08/2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CalendarConfiguration.class})
public class ExampleCalendarFromCsvFile {

    @Autowired
    private LocalDateCalendar calendar;

    @BeforeClass
    public static void beforeClass() throws Exception {
        setProperty("calendarFileLocation", new ClassPathResource("dates").getURL().getPath());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        clearProperty("calendarFileLocation");
    }

    @Test
    public void findDate() {
        assertThat(calendar.getEndDate(), is(LocalDate.of(2017,12,30)));
        assertThat(calendar.getAllDates().size(), is(5));
    }
}