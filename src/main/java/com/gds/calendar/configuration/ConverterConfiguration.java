package com.gds.calendar.configuration;

import com.gds.calendar.LocalDateCalendar;
import org.apache.commons.csv.CSVFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import static org.springframework.util.Assert.notNull;

/**
 * @author Matt Vickery (matt.d.vickery@greendotsoftware.co.uk)
 * @since 08/08/2017
 */
@Import(PropertiesConfiguration.class)
@Configuration
@PropertySource(value = {
        "classpath:properties/calendar.properties",
        "file:${calendar:calendar.properties}"
}, ignoreResourceNotFound = true)
public class ConverterConfiguration implements EnvironmentAware {

    public static final String CALENDAR_FILE_LOCATION = "calendarFileLocation";

    private Environment environment;
    @Value("${datePattern}")
    private String datePattern;
    @Value("${calendarStartDate}")
    private String calendarStartDate;
    @Value("${calendarDuration}")
    private int calendarDuration;

    @Bean
    public ConversionServiceFactoryBean conversionService() {

        final ConversionServiceFactoryBean factory = new ConversionServiceFactoryBean();
        factory.setConverters(new HashSet<Converter>() {{
            add(stringToLocalDateConverter());
            add(dateCollectionSourceToCalendarConverter());
        }});
        return factory;
    }

    @Bean
    public Converter<String, LocalDate> stringToLocalDateConverter() {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(final String date) {
                notNull(date, "Mandatory argument 'date' is missing");
                return LocalDate.parse(date.trim(), DateTimeFormatter.ofPattern(datePattern));
            }
        };
    }

    @Bean
    public Converter<String, LocalDateCalendar> dateCollectionSourceToCalendarConverter() {

        return new Converter<String, LocalDateCalendar>() {
            @Override
            public LocalDateCalendar convert(final String dateCollectionSource) {
                notNull(dateCollectionSource, "Mandatory argument 'dateCollectionSource' is missing");
                final LocalDateCalendar calendar = new LocalDateCalendar(
                        stringToLocalDateConverter().convert(calendarStartDate), calendarDuration)
                        .removeWeekDays().removeWeekendDays();
                try (final Reader reader = new BufferedReader(new FileReader(
                        new File(holidayFileLocation(), dateCollectionSource)))) {
                    CSVFormat.DEFAULT.parse(reader).getRecords().stream().forEach(
                            record -> record.forEach(
                                    value -> calendar.add(stringToLocalDateConverter().convert(value))
                            ));
                    return calendar;
                } catch (IOException e) {
                    throw new IllegalStateException("Directory or file name correct?", e);
                }
            }
        };
    }

    private String holidayFileLocation() {
        final String holidayFileLocation = environment.getProperty(CALENDAR_FILE_LOCATION);
        if (holidayFileLocation == null)
            throw new IllegalStateException("Holiday file location [" + CALENDAR_FILE_LOCATION + "] is null.");
        return holidayFileLocation;
    }

    public void setEnvironment(final Environment environment) {
        notNull(environment, "Mandatory argument 'environment' is missing");
        this.environment = environment;
    }
}