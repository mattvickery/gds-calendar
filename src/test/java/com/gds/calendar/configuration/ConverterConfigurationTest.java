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
    @Value("${calendarStartDate}")
    private String calendarStartDate;
    @Value("${calendarDuration}")
    private int calendarDuration;

    @Resource(name = "stringToLocalDateConverter")
    private Converter<String, LocalDate> stringLocalDateConverter;

    @Resource(name = "dateCollectionSourceToCalendarConverter")
    private Converter<String, LocalDateCalendar> dateCollectionSourceToCalendarConverter;

    private String calendarDatesFileName = "calendar-dates.csv";

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty(ConverterConfiguration.CALENDAR_LOCATION,
                new ClassPathResource("dates").getURL().getPath());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.clearProperty(ConverterConfiguration.CALENDAR_LOCATION);
    }

    @Test
    public void stringToDateConverter() {
        final LocalDate convertedDate = stringLocalDateConverter.convert(calendarStartDate);
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