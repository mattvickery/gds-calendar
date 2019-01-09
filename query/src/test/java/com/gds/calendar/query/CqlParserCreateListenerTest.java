package com.gds.calendar.query;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

/**
 *  Grammar needs to be updated to support quoted dates, i.e. '1/12/2000' instead of 1/12/2000.
 */
public class CqlParserCreateListenerTest {

    private final CqlParserCreateListener parserCreateListener = new CqlParserCreateListener();
    private final Map<String, Object> tokenValues = new HashMap<>();

    {
        parserCreateListener.addPropertyChangeListener(e -> {
//            System.out.println(e.getPropertyName()+"::"+e.getNewValue());
            tokenValues.put(e.getPropertyName(), e.getNewValue());
        });
    }

    @Before
    public void before() {
        tokenValues.clear();
    }

    @Test
    public void parse_validSimpleQuery() {

        createParserFor("create calendar 'businessCalendar' start 1/11/2008 duration 2 years")
                .create_calendar_stmt();
        assertThat(tokenValues, hasEntry("statementType","create"));
        assertThat(tokenValues, hasEntry("calendarIdentifier","businessCalendar"));
        //...
    }

    @Test
    public void parse_validSimpleQueryWithWeekendsFilter() {

        try {
            createParserFor("create alendar 'myCalendar' start 01/11/2008 duration 2 years without_weekends")
                .create_calendar_stmt();
            assertThat(tokenValues, hasEntry("statementType", "create"));
            assertThat(tokenValues, hasEntry("calendarIdentifier", "myCalendar"));
            //...
        }
        catch(RecognitionException e) {
            System.out.println('x');
        }
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
}