# GDS Calendar

Wiki: https://github.com/mattvickery/gds-com.gds.com.gds.calendar/wiki

CI: https://travis-ci.org/mattvickery/gds-com.gds.com.gds.calendar

# Modules

## Core

All of the calendar and utility classes required to support calendar management.

## Client

A client SDK that includes a criteria API and a free-format query facility that can be used against calendars.
* Queries are parsed for correctness by the client.
* The SDK is versioned and released independently but maintain backwards compatibility.
* Calendars can be treated as resources with complete life-cycle operations.

## Service

A deployable micro-service implementation of named calendars that can be queried, obtained and managed by 
external parties. Calendars can be created and managed by:
* Client SDK Java API.
* Direct ReST API access.
* Server-side properties files.
* Database backed server instance.

## Query

CQL grammar and supporting code required for query lexical analysis and parsing.
