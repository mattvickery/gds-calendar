parser grammar CqlParser;

@header {
    package com.gds.calendar.query;
}

options { tokenVocab=CqlLexer; }

sql_stmt
    : ( create_calendar_stmt | delete_calendar_stmt)
    ;

create_calendar_stmt:
    CREATE CALENDAR calendar_identifier START date_identifer duration filters?
    ;

filters
    : ( WITHOUT_WEEKENDS |
        WITHOUT_WEEKDAYS |
        WITH_HOLIDAYS ( date_identifer ) ( COMMA date_identifer ) * )
    ;

delete_calendar_stmt
    : DELETE CALENDAR calendar_identifier
    ;

duration
    : DURATION ORDINAL (DAYS | YEARS)
    ;

calendar_identifier
    : STRING_LITERAL
    ;

date_identifer
    : DATE
    ;
