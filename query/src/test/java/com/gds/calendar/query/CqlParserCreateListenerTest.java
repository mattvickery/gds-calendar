package com.gds.calendar.query;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

public class CqlParserCreateListenerTest {

    private final CqlParserCreateListener parserCreateListener = new CqlParserCreateListener();
    private final Map<String, Object> tokenValues = new HashMap<>();

    {
        parserCreateListener.addPropertyChangeListener(this::propertyChange);
    }

    @Before
    public void before() {
        tokenValues.clear();
    }

    @Test
    public void parse_validSimpleQuery() {

        createParserFor("create calendar 'businessCalendar' start '1/8/2008' duration 2 years")
            .create_calendar_stmt();
        assertThat(tokenValues, hasEntry("statementType", "create"));
        assertThat(tokenValues, hasEntry("calendarIdentifier", "businessCalendar"));
        assertThat(tokenValues, hasEntry("date", "1/8/2008"));
        //...
    }

    @Test
    public void parse_validSimpleQueryWithWeekendsFilter() {

        createParserFor("create calendar 'myCalendar' start '01/01/2008' duration 2 years without_weekends")
            .create_calendar_stmt();
        assertThat(tokenValues, hasEntry("statementType", "create"));
        assertThat(tokenValues, hasEntry("calendarIdentifier", "myCalendar"));
        assertThat(tokenValues, hasEntry("date", "01/01/2008"));
        //...
    }

    private CqlParser createParserFor(final String query) {

        final Lexer lexer = new CqlLexer(new ANTLRInputStream(query));
        final List<String> errors =  new ArrayList<>();
        final AggregatedErrorReportingListener errorReportingListener = new AggregatedErrorReportingListener();
        lexer.addErrorListener(errorReportingListener.registerErrorConsumer(errors::add));

        if (errors.isEmpty()) {
            final CqlParser parser = new CqlParser(new CommonTokenStream(lexer));
            parser.addParseListener(parserCreateListener);
            return parser;
        }
        throw new IllegalArgumentException("it failed....");
    }

    private void propertyChange(PropertyChangeEvent e) {
        tokenValues.put(e.getPropertyName(), e.getNewValue());
    }
}