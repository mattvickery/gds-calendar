# GDS Calendar

Wiki: https://github.com/mattvickery/gds-com.gds.com.gds.calendar/wiki

CI: https://travis-ci.org/mattvickery/gds-calendar

# Modules

## Core

Core calendar classes only, this module can be used in stand-alone applications and should not carry any baggage that is not required by that type of application. 

## Client

A client SDK that includes a criteria API and a free-format query facility that can be used against calendars.
* Queries are parsed for correctness by the client.
* The SDK is versioned and released independently but maintain backwards compatibility.
* Calendars can be treated as resources with complete life-cycle operations, each calendar has a name and an ID.

## Service

A deployable micro-service implementation of named calendars that can be queried, obtained and managed by 
external parties. Calendars can be created and managed by:
* Client SDK Java API.
* Direct ReST API access.
* Server-side properties files.
* Database backed server instance.
* CQL Query Submission

The command server will be located in this module.

## Query

CQL grammar and supporting code required for query lexical analysis and parsing. Both Lexer and Parser listeners will be included in this module in order that various client code can take advantage of using them.
