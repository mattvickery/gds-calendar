package com.gds.calendar.query;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
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

        createParserFor("create calendar 'myCalendar' start 01/11/2008 duration 2 years without_weekends")
                .create_calendar_stmt();
        assertThat(tokenValues, hasEntry("statementType","create"));
        assertThat(tokenValues, hasEntry("calendarIdentifier","myCalendar"));
        //...
    }

    private CqlParser createParserFor(final String query) {

        final Lexer lexer = new CqlLexer(new ANTLRInputStream(query));
        final CqlParser parser = new CqlParser(new CommonTokenStream(lexer));
        parser.addParseListener(parserCreateListener);

        return parser;
    }
}