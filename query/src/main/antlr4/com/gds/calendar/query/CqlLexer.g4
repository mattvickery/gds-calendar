lexer grammar CqlLexer;

SPACES
 : [ \u000B\t\r\n] -> channel(HIDDEN)
 ;

SCOL : ';';
DOT : '.';
OPEN_PAR : '(';
CLOSE_PAR : ')';
COMMA : ',';
ASSIGN : '=';
STAR : '*';
PLUS : '+';
MINUS : '-';
TILDE : '~';
PIPE2 : '||';
DIV : '/';
MOD : '%';
LT2 : '<<';
GT2 : '>>';
AMP : '&';
PIPE : '|';
LT : '<';
LT_EQ : '<=';
GT : '>';
GT_EQ : '>=';
EQ : '==';
NOT_EQ1 : '!=';
NOT_EQ2 : '<>';
UNDER : '_';

fragment DIGIT : [0-9];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

SELECT: S E L E C T;
UPDATE: U P D A T E;
CREATE: C R E A T E;
DELETE: D E L E T E;

FROM: F R O M;
WHERE: W H E R E;
ORDER: O R D E R;
BY: B Y;
AND: A N D;
OR: O R;

ADD: A D D;
HOLIDAY: H O L I D A Y;
CALENDAR: C A L E N D A R;
WEEKDAYS: W E E K D A Y S;
WEEKENDS: W E E K E N D S;
DAY: D A Y;
REMOVE: R E M O V E;
WITHOUT_WEEKENDS: W I T H O U T UNDER W E E K E N D S;
WITHOUT_WEEKDAYS: W I T H O U T UNDER W E E K D A Y S;
WITH_HOLIDAYS: W I T H UNDER H O L I D A Y S;
START: S T A R T;
ENDS_WITH: E N D S UNDER W I T H;
DURATION: D U R A T I O N;
DAYS: D A Y S;
YEARS: Y E A R S;

ORDINAL
    :
    DIGIT+
    ;

SINGLE_LINE_COMMENT
    : '--' ~[\r\n]* -> channel(HIDDEN)
    ;

MULTILINE_COMMENT
    : '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN)
    ;

STRING_LITERAL
    : '\'' ( ~'\'' | '\'\'' )* '\''
    ;

YEAR
    : DIGIT+
    ;
    
MONTH_NUMERIC
    : DIGIT [1-9]?
    ;

DAY_OF_MONTH
    :
    DIGIT [1-9]?
    ;

DATE
    : DAY_OF_MONTH '/' MONTH_NUMERIC '/' YEAR
    ;

