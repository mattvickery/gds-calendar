parser grammar CqlParser;

@header {
    package com.gds.calendar.query;
}

options { tokenVocab=CqlLexer; }

sql_stmt
    : ( create_calendar_stmt | delete_calendar_stmt | update_calendar_stmt )
    ;

create_calendar_stmt:
    CREATE CALENDAR calendar_identifier START date_identifer duration filters?
    ;

delete_calendar_stmt
    : DELETE CALENDAR calendar_identifier
    ;

update_calendar_stmt
    : UPDATE CALENDAR calendar_identifier ( REMOVE | ADD ) ( WEEKDAYS | WEEKENDS | ( DAY date_identifer) )
    ;

filters
    : ( WITHOUT_WEEKENDS |
        WITHOUT_WEEKDAYS |
        WITH_HOLIDAYS ( date_identifer ) ( COMMA date_identifer ) * )
    ;

duration
    : DURATION ORDINAL ( DAYS | YEARS )
    ;

calendar_identifier
    : STRING_LITERAL
    ;

date_identifer
    : DATE
    ;
