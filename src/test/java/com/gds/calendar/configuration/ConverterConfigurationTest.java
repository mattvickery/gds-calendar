package com.gds.calendar.configuration;

import com.gds.calendar.LocalDateCalendar;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 08/08/2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConverterConfiguration.class})
public class ConverterConfigurationTest {

    @Value("${datePattern}")
    private String datePattern;
    @Value("${calendarDuration}")
    private int calendarDuration;

    @Resource(name = "stringToLocalDateConverter")
    private Converter<String, LocalDate> stringLocalDateConverter;

    @Resource(name = "dateCollectionSourceToCalendarConverter")
    private Converter<String, LocalDateCalendar> dateCollectionSourceToCalendarConverter;

    private String calendarDatesFileName = "calendar-dates.csv";

    @BeforeClass
    public static void beforeClass() throws IOException {
        System.setProperty(ConverterConfiguration.CALENDAR_DATES_LOCATION_PROPERTY_NAME,
                new ClassPathResource("dates").getURL().getPath());
        System.setProperty(ConverterConfiguration.CALENDAR_PROPERTIES_PROPERTY_NAME,
                new ClassPathResource("properties").getURL().getPath());
        System.setProperty("calendarEndDate", "2017-12-30");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty(ConverterConfiguration.CALENDAR_DATES_LOCATION_PROPERTY_NAME);
        System.clearProperty(ConverterConfiguration.CALENDAR_PROPERTIES_PROPERTY_NAME);
        System.clearProperty("calendarEndDate");
    }

    @Test
    public void stringToDateConverter() {

        final LocalDate convertedDate = stringLocalDateConverter.convert("2017-12-30");
        assertThat(convertedDate.getDayOfMonth(), is(30));
        assertThat(convertedDate.getMonthValue(), is(12));
        assertThat(convertedDate.getYear(), is(2017));
    }

    @Test
    public void dateCollectionSourceToCalendarConverter() {

        final LocalDateCalendar calendar =
                dateCollectionSourceToCalendarConverter.convert(calendarDatesFileName);
        assertThat(calendar, notNullValue());
        assertThat(calendar.getDayBefore(calendar.getEndDate()).get(), equalTo(LocalDate.of(2017,12,25)));
    }
}