package com.gds.calendar.query;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.util.StringUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Optional;


public class CqlParserCreateListener extends CqlParserBaseListener {

    private PropertyChangeSupport propertyChangeSupport;

    public CqlParserCreateListener() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
        propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitCalendar_identifier(@NotNull CqlParser.Calendar_identifierContext ctx) {
        Optional.ofNullable(ctx.STRING_LITERAL())
            .ifPresent(ci -> propertyChangeSupport
                .firePropertyChange("calendarIdentifier", null, replaceQuotes(ci.toString())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterCreate_calendar_stmt(@NotNull CqlParser.Create_calendar_stmtContext ctx) {
        propertyChangeSupport.firePropertyChange("statementType", null, "create");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDuration(@NotNull CqlParser.DurationContext ctx) {
        System.out.println("Entering enterDuration");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDuration(@NotNull CqlParser.DurationContext ctx) {
        System.out.println("Entering exitDuration");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDate_identifer(@NotNull CqlParser.Date_identiferContext ctx) {
        System.out.println("Entering enterDate_identifer");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDate_identifer(@NotNull CqlParser.Date_identiferContext ctx) {
        Optional.ofNullable(ctx.DATE())
            .ifPresent(date -> propertyChangeSupport.firePropertyChange("date", null, replaceQuotes(date.toString())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterFilters(CqlParser.FiltersContext ctx) {
        System.out.println("Entering enterFilters");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitFilters(CqlParser.FiltersContext ctx) {
        System.out.println("Entering exitFilters");
    }

    private static String replaceQuotes(final String str) {
        return str == null ? null : str.replace("'", "");
    }
}