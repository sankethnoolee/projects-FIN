grammar ValidationGrammar;
r		: exp  EOF;

exp		: forEach
		| logicalExp
		| dateFunction
		| stringFunction
		| numericFunction
		| lookup;

dateFunction		: OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES 
					| ddEntityColName
					| meColName
					| PERIOD
					| 'SOM' OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES
					| 'EOM' OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES
					| 'SOY' OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES
					| 'EOY' OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES
					| 'SOFY' OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES
					| 'EOFY' OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES
					| 'SOQ' OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES
					| 'EOQ' OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES
					| CurrentPeriod
					| lookup
					| max
					| min
					| unique
					| 'TODATE' OPEN_PARENTHESES (  ddEntityColName | meColName | DATEPERIOD) CLOSE_PARENTHESES;
					
numericFunction		: OPEN_PARENTHESES numericFunction CLOSE_PARENTHESES
					| DATEPERIOD | DATEPERIODNEG
					| INT | INTNEG
					| DOUBLE | DOUBLENEG 
					| ddEntityColName
					| meColName
					| len 
					| sumifStatement 
					| numericFunction (PLUS | (MINUS)? |MUL | DIV ) numericFunction
					| DATEPART OPEN_PARENTHESES dateFunction COMMA ( ( DOUBLE_QUOTE ('D'|'d') DOUBLE_QUOTE ) | ( DOUBLE_QUOTE ('M'|'m') DOUBLE_QUOTE ) | ( DOUBLE_QUOTE ('Y'|'y') DOUBLE_QUOTE ) )  CLOSE_PARENTHESES
					| DATEDIFF OPEN_PARENTHESES dateFunction  COMMA dateFunction COMMA ( ( DOUBLE_QUOTE ('D'|'d') DOUBLE_QUOTE ) | ( DOUBLE_QUOTE ('M'|'m') DOUBLE_QUOTE ) | ( DOUBLE_QUOTE ('Y'|'y') DOUBLE_QUOTE )  ) CLOSE_PARENTHESES
					| lookup
					| avg			
					| round		
					| floor		
					| ceil		
					| abs			
					| percentage
					| count
					| dcount
					| sum
					| max
					| min
					| maxifStatement
					| minifStatement
					| countifStatment
					| unique
					| CONVERT OPEN_PARENTHESES stringFunction COMMA DOUBLE_QUOTE NUMBER DOUBLE_QUOTE CLOSE_PARENTHESES;
					
stringFunction		: OPEN_PARENTHESES stringFunction CLOSE_PARENTHESES
					| stringLiterals
					| ddEntityColName
					| meColName
					| (SUBSTR OPEN_PARENTHESES	stringFunction COMMA numericFunction COMMA numericFunction CLOSE_PARENTHESES)
					| (LOWER OPEN_PARENTHESES	stringFunction CLOSE_PARENTHESES)
					| (UPPER OPEN_PARENTHESES	stringFunction CLOSE_PARENTHESES)
					| (TRIM OPEN_PARENTHESES	stringFunction CLOSE_PARENTHESES)
					| (REPLACE OPEN_PARENTHESES	stringFunction COMMA stringFunction COMMA stringFunction CLOSE_PARENTHESES)
					| (COALESCE OPEN_PARENTHESES	stringFunction COMMA (stringFunction | numericFunction) CLOSE_PARENTHESES)
					| (CONCAT OPEN_PARENTHESES( meColName | ddEntityColName) ( COMMA (  meColName | ddEntityColName))+ CLOSE_PARENTHESES)
					| (CONCAT OPEN_PARENTHESES(stringFunction ) ( COMMA (stringFunction  ))+ CLOSE_PARENTHESES)
					| (CONCAT OPEN_PARENTHESES( dateFunction ) ( COMMA ( dateFunction  ))+ CLOSE_PARENTHESES)
					| (CONCAT OPEN_PARENTHESES( numericFunction ) ( COMMA ( numericFunction  ))+ CLOSE_PARENTHESES)
					| lookup
					| max
					| min
					| unique
					| CONVERT OPEN_PARENTHESES numericFunction COMMA DOUBLE_QUOTE STRING DOUBLE_QUOTE CLOSE_PARENTHESES;
					
ifStatement 		: IF OPEN_PARENTHESES logicalExp CLOSE_PARENTHESES THEN OPEN_PARENTHESES logicalExp CLOSE_PARENTHESES (ELSEIF OPEN_PARENTHESES logicalExp CLOSE_PARENTHESES THEN OPEN_PARENTHESES logicalExp CLOSE_PARENTHESES)* ELSE OPEN_PARENTHESES logicalExp CLOSE_PARENTHESES ;

sumifStatement 		: 	SUMIF OPEN_PARENTHESES ( ddEntityColName | meColName)  ( COMMA(  OPEN_SQR_BRACKETS (  ddEntityColName | meColName | NOGROUPBY_KW  |  ddEntityColNameWithAlias | meColNameWithAlias ) ( COMMA (  ddEntityColName | meColName  | ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS  )? ( COMMA OPEN_SQR_BRACKETS ( logicalExp ) ( COMMA ( logicalExp ) )* CLOSE_SQR_BRACKETS  )?)? CLOSE_PARENTHESES;

maxifStatement 		: 	MAXIF OPEN_PARENTHESES ( ddEntityColName | meColName) ( COMMA OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW  | ddEntityColNameWithAlias | meColNameWithAlias ) ( COMMA ( ddEntityColName | meColName  | ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS  )? ( COMMA OPEN_SQR_BRACKETS ( logicalExp ) ( COMMA ( logicalExp ) )* CLOSE_SQR_BRACKETS  )? CLOSE_PARENTHESES;

minifStatement 		: 	MINIF OPEN_PARENTHESES ( ddEntityColName |meColName) ( COMMA OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW  | ddEntityColNameWithAlias | meColNameWithAlias  ) ( COMMA ( ddEntityColName | meColName  | ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS  )? ( COMMA OPEN_SQR_BRACKETS ( logicalExp ) ( COMMA ( logicalExp ) )* CLOSE_SQR_BRACKETS  )? CLOSE_PARENTHESES;

countifStatment		: 	COUNTIF OPEN_PARENTHESES ( ddEntityColName | meColName )  ( COMMA (  OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW ) ( COMMA ( ddEntityColName | meColName ) )* CLOSE_SQR_BRACKETS  )? (COMMA (  OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW  | ddEntityColNameWithAlias | meColNameWithAlias ) ( COMMA ( ddEntityColName | meColName  | ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS  )?)? ( COMMA OPEN_SQR_BRACKETS ( logicalExp ) ( COMMA ( logicalExp ) )* CLOSE_SQR_BRACKETS  )?)? CLOSE_PARENTHESES;

betweenStatment		:	(OPEN_PARENTHESES numericFunction CLOSE_PARENTHESES BETWEEN OPEN_SQR_BRACKETS numericFunction COMMA numericFunction CLOSE_SQR_BRACKETS) 
					| 	(OPEN_PARENTHESES dateFunction CLOSE_PARENTHESES BETWEEN OPEN_SQR_BRACKETS dateFunction COMMA dateFunction CLOSE_SQR_BRACKETS) ;

// Add grammar for conditions like COLUMN <=|in|NotIn> BusinessString Constant
logicalExp			:  ( meColName | ddEntityColName) RELOPTS ( meColName | ddEntityColName) 
					| lookup RELOPTS ( meColName | ddEntityColName)
					| OPEN_PARENTHESES logicalExp CLOSE_PARENTHESES					
					| ifStatement 
					| (numericFunction RELOPTS numericFunction)
					| (dateFunction RELOPTS dateFunction) 		
					| (stringFunction RELOPTS  stringFunction)					
					| and
					| or
					| in
					| notIn
					| not
					| contains
					| doesNotContains
					| begins
					| ends
					| isempty
					| isnotempty
					| betweenStatment
					| lookup
					| TRUE 
					| FALSE
					| REGEX_KW OPEN_PARENTHESES stringFunction COMMA DOUBLE_QUOTE (. | '\\')+ DOUBLE_QUOTE CLOSE_PARENTHESES;
						
len 				: LEN OPEN_PARENTHESES stringFunction CLOSE_PARENTHESES;



// lookup return value can be used in arthemetic and relational operation
lookup				: LOOKUP_KW OPEN_PARENTHESES ( ME_KW | ddEntityColName | lookup)? COMMA ( ddEntityColName) COMMA ( OPEN_SQR_BRACKETS ((  ddEntityColName | meColName) RELOPTS ( ddEntityColName | meColName) ) ( COMMA (( ddEntityColName | meColName) RELOPTS ( ddEntityColName | meColName) ))* CLOSE_SQR_BRACKETS)? (COMMA(  ( OPEN_SQR_BRACKETS ( ddEntityColName)*(COMMA ( ddEntityColName | meColName))* CLOSE_SQR_BRACKETS)? ) ?)? (COMMA( (OPEN_SQR_BRACKETS (logicalExp) 
					  (COMMA logicalExp)* CLOSE_SQR_BRACKETS)?)?)? CLOSE_PARENTHESES;

forEach				: FOREACH_KW OPEN_PARENTHESES ( ddEntityColName) ( COMMA (  OPEN_SQR_BRACKETS ( ddEntityColName)  ( COMMA ( ddEntityColName))* CLOSE_SQR_BRACKETS)? (COMMA (  OPEN_SQR_BRACKETS (exp) ( COMMA (exp))* CLOSE_SQR_BRACKETS)? )?)? CLOSE_PARENTHESES OPEN_BRACES exp CLOSE_BRACES;

and					: AND_KW OPEN_PARENTHESES logicalExp (COMMA logicalExp)+ CLOSE_PARENTHESES;
or					: OR_KW OPEN_PARENTHESES logicalExp (COMMA logicalExp)+ CLOSE_PARENTHESES;
not					: NOT_KW OPEN_PARENTHESES logicalExp CLOSE_PARENTHESES;
notIn				: ((dateFunction)   NOTIN_KW OPEN_SQR_BRACKETS  (DATE)( COMMA DATE)* CLOSE_SQR_BRACKETS )|( (stringFunction)   NOTIN_KW OPEN_SQR_BRACKETS  (stringLiterals)( COMMA stringLiterals)* CLOSE_SQR_BRACKETS )|(  (numericFunction)   NOTIN_KW OPEN_SQR_BRACKETS  ( (INT |  INTNEG| DOUBLE | DOUBLENEG))(COMMA  (INT |  INTNEG| DOUBLE | DOUBLENEG) )* CLOSE_SQR_BRACKETS ) ;
in					: ((dateFunction)   IN_KW OPEN_SQR_BRACKETS  (DATE)( COMMA DATE)* CLOSE_SQR_BRACKETS )|((stringFunction)   IN_KW OPEN_SQR_BRACKETS  (stringLiterals)( COMMA stringLiterals)* CLOSE_SQR_BRACKETS )|(  (numericFunction)   IN_KW OPEN_SQR_BRACKETS  ( (INT |  INTNEG| DOUBLE | DOUBLENEG))(COMMA  (INT |INTNEG| DOUBLE | DOUBLENEG) )* CLOSE_SQR_BRACKETS ) ;
contains			: OPEN_PARENTHESES stringFunction CLOSE_PARENTHESES CONTAINS_KW OPEN_SQR_BRACKETS  (stringLiterals)(COMMA stringLiterals)* CLOSE_SQR_BRACKETS ;
doesNotContains			: OPEN_PARENTHESES stringFunction CLOSE_PARENTHESES DOES_NOT_CONTAINS_KW OPEN_SQR_BRACKETS  (stringLiterals)(COMMA stringLiterals)* CLOSE_SQR_BRACKETS ;
begins				: OPEN_PARENTHESES stringFunction CLOSE_PARENTHESES BEGINS_KW  OPEN_SQR_BRACKETS  (stringLiterals)(COMMA stringLiterals)* CLOSE_SQR_BRACKETS ;
ends				: OPEN_PARENTHESES stringFunction CLOSE_PARENTHESES ENDS_KW OPEN_SQR_BRACKETS  (stringLiterals)(COMMA stringLiterals)* CLOSE_SQR_BRACKETS ;
isempty				: ISEMPTY_KW OPEN_PARENTHESES ( ddEntityColName | meColName | stringFunction | dateFunction | numericFunction ) CLOSE_PARENTHESES;
isnotempty			: ISNOTEMPTY_KW OPEN_PARENTHESES ( ddEntityColName | meColName |  stringFunction | dateFunction | numericFunction ) CLOSE_PARENTHESES;
unique				: UNIQUE_KW OPEN_PARENTHESES ( ddEntityColName | meColName )  ( COMMA (  OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW ) ( COMMA ( ddEntityColName | meColName ) )* CLOSE_SQR_BRACKETS  )? (COMMA (  OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW  | ddEntityColNameWithAlias | meColNameWithAlias ) ( COMMA ( ddEntityColName | meColName  | ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS  )?)? ( COMMA OPEN_SQR_BRACKETS ( logicalExp ) ( COMMA ( logicalExp ) )* CLOSE_SQR_BRACKETS  )?)? CLOSE_PARENTHESES;

sum					: SUM_KW OPEN_PARENTHESES ( ddEntityColName | meColName | numericFunction)  ( COMMA OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW |  ddEntityColNameWithAlias | meColNameWithAlias ) ( COMMA ( ddEntityColName | meColName  |  ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS )? CLOSE_PARENTHESES ;
max					: MAX_KW OPEN_PARENTHESES ( ddEntityColName  | meColName | numericFunction) ( COMMA OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW|  ddEntityColNameWithAlias | meColNameWithAlias ) ( COMMA (  ddEntityColName | meColName  |  ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS )? CLOSE_PARENTHESES;
min					: MIN_KW OPEN_PARENTHESES ( ddEntityColName  | meColName | numericFunction) ( COMMA OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW  |  ddEntityColNameWithAlias | meColNameWithAlias) ( COMMA ( ddEntityColName | meColName | ddEntityColNameWithAlias | meColNameWithAlias ) )* CLOSE_SQR_BRACKETS )? CLOSE_PARENTHESES;
count				: COUNT_KW OPEN_PARENTHESES ( ddEntityColName | meColName )  ( COMMA (  OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW ) ( COMMA ( ddEntityColName | meColName ) )* CLOSE_SQR_BRACKETS  )? (COMMA (  OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW  |  ddEntityColNameWithAlias | meColNameWithAlias ) ( COMMA ( ddEntityColName | meColName  |  ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS  )?)? ( COMMA OPEN_SQR_BRACKETS ( logicalExp ) ( COMMA ( logicalExp ) )* CLOSE_SQR_BRACKETS  )?)? CLOSE_PARENTHESES;
dcount				: DCOUNT_KW OPEN_PARENTHESES ( ddEntityColName | meColName )  ( COMMA (  OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW ) ( COMMA ( ddEntityColName | meColName ) )* CLOSE_SQR_BRACKETS  )? (COMMA (  OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW  |  ddEntityColNameWithAlias | meColNameWithAlias ) ( COMMA ( ddEntityColName | meColName  | ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS  )?)? ( COMMA OPEN_SQR_BRACKETS ( logicalExp ) ( COMMA ( logicalExp ) )* CLOSE_SQR_BRACKETS  )?)? CLOSE_PARENTHESES;
//pct2total			: PCT2TOTAL_KW OPEN_PARENTHESES ( ddEntityColName ) CLOSE_PARENTHESES;
avg					: AVG_KW OPEN_PARENTHESES ( ddEntityColName | meColName)  ( COMMA(  OPEN_SQR_BRACKETS ( ddEntityColName | meColName | NOGROUPBY_KW  |  ddEntityColNameWithAlias | meColNameWithAlias ) ( COMMA ( ddEntityColName | meColName  |  ddEntityColNameWithAlias | meColNameWithAlias) )* CLOSE_SQR_BRACKETS  )? ( COMMA OPEN_SQR_BRACKETS ( logicalExp ) ( COMMA ( logicalExp ) )* CLOSE_SQR_BRACKETS  )?)? CLOSE_PARENTHESES;
round				: ROUND_KW OPEN_PARENTHESES ( ddEntityColName | meColName | numericFunction) COMMA (numericFunction) CLOSE_PARENTHESES;
floor				: FLOOR_KW OPEN_PARENTHESES ( ddEntityColName | meColName | numericFunction) COMMA (numericFunction) CLOSE_PARENTHESES;
ceil				: CEIL_KW OPEN_PARENTHESES ( ddEntityColName | meColName | numericFunction) COMMA (numericFunction) CLOSE_PARENTHESES;
abs					: ABS_KW OPEN_PARENTHESES ( ddEntityColName | meColName | numericFunction) CLOSE_PARENTHESES;
percentage			: PERCENTAGE_KW OPEN_PARENTHESES ( ddEntityColName | meColName) CLOSE_PARENTHESES;
stringLiterals		: DOUBLE_QUOTE ( 'Y' | 'M' | 'D' | BusinessString|INT|DOUBLE |INTNEG|DOUBLENEG |DATEPERIOD|DATEPERIODNEG|BusinessString | TRUE | FALSE | PLUS | MINUS |MUL | DIV  | OPEN_BRACES | CLOSE_BRACES | BACKSLASH |SINGLE_QUOTE)+ DOUBLE_QUOTE
					| DATE;

LOOKUP_KW			: (Space)*V L O O K U P (Space)*;
ME_KW				: (Space)* M E (Space)*; 
FOREACH_KW			: (Space)* F O R E A C H (Space)*;
AND_KW				: (Space)* A N D (Space)*;
OR_KW				: (Space)* O R (Space)*;
NOT_KW				: (Space)* N O T (Space)*;
IN_KW				: (Space)* I N (Space)*;
NOTIN_KW			: (Space)* N O T I N (Space)*;
CONTAINS_KW			: (Space)* C O N T A I N S (Space)*;
DOES_NOT_CONTAINS_KW: (Space)* D O E S N O T C O N T A I N S (Space)*;
BEGINS_KW			: (Space)* B E G I N S W I T H (Space)*;
ENDS_KW				: (Space)* E N D S W I T H (Space)*;
ISEMPTY_KW			: (Space)* I S E M P T Y (Space)*;
ISNOTEMPTY_KW		: (Space)* I S N O T E M P T Y (Space)*;
UNIQUE_KW			: (Space)* U N I Q U E (Space)*;
SUM_KW				: (Space)* S U M (Space)*;
MAX_KW				: (Space)* M A X (Space)*;
MIN_KW				: (Space)* M I N (Space)*;
COUNT_KW			: (Space)* C O U N T (Space)*;
DCOUNT_KW			: (Space)* D C O U N T (Space)*;
PCT2TOTAL_KW		: (Space)* P C T '2' T O T A L (Space)*;
AVG_KW				: (Space)* A V G (Space)*;
ROUND_KW			: (Space)* R O U N D (Space)*;
FLOOR_KW			: (Space)* F L O O R (Space)*;
CEIL_KW				: (Space)* C E I L (Space)*;
ABS_KW				: (Space)* A B S (Space)*;
PERCENTAGE_KW		: (Space)* P E R C (Space)*;
REGEX_KW			: (Space)* R E G E X (Space)*;
LEN 				: (Space)* L E N (Space)*;
IF					: (Space)* I F (Space)*;
THEN				: (Space)* T H E N (Space)*;
ELSE				: (Space)* E L S E (Space)*;
SUMIF				: (Space)* S U M I F (Space)*;
RTN					: (Space)* R T N (Space)*;
REFTBL				: (Space)* R E F T B L (Space)*;
ENT				    : (Space)* E N T (Space)*;
AND					: (Space)* A N D (Space)*;
SUBSTR				: (Space)* S U B S T R (Space)*;
LOWER				: (Space)* L O W E R (Space)*;
UPPER				: (Space)* U P P E R (Space)*;
TRIM				: (Space)* T R I M (Space)*;
REPLACE				: (Space)* R E P L A C E (Space)*;
COALESCE			: (Space)* C O A L E S C E (Space)*;
CONCAT				: (Space)* C O N C A T (Space)*;
DATEPART			: (Space)* D A T E P A R T (Space)*;
DATEDIFF			: (Space)* D A T E D I F F (Space)*;
FALSE				: (Space)* F A L S E (Space)*;
TRUE				: (Space)* T R U E (Space)*;
BETWEEN				: (Space)* B E T W E E N (Space)*;
MAXIF				: (Space)* M A X I F (Space)*;
MINIF				: (Space)* M I N I F (Space)*;
COUNTIF				: (Space)* C O U N T I F (Space)*;
CONVERT				: (Space)* C O N V E R T (Space)*;
STRING 				: (Space)* S T R I N G (Space)*;
NUMBER				: (Space)* N U M B E R (Space)*;
ELSEIF				: (Space)* E L S E I F (Space)*;


NOGROUPBY_KW		: '$Aggregate';

ddEntityColName     : ENT DOT DOUBLE_QUOTE (column_spcl_char | BusinessString)+ DOUBLE_QUOTE DOT DOUBLE_QUOTE (column_spcl_char | BusinessString)+ DOUBLE_QUOTE (DOT DOUBLE_QUOTE (column_spcl_char | BusinessString)+ DOUBLE_QUOTE)? (OPEN_PARENTHESES ((column_spcl_char | BusinessString)+) CLOSE_PARENTHESES)? ( OPEN_PARENTHESES ((PERIOD)? ( COMMA ((INT | INTNEG | BusinessString ))? COMMA ((INT | INTNEG | BusinessString ))? )?) CLOSE_PARENTHESES)?;

ddEntityColNameWithAlias : ENT DOT DOUBLE_QUOTE (column_spcl_char | BusinessString)+ DOUBLE_QUOTE DOT DOUBLE_QUOTE (column_spcl_char | BusinessString)+ DOUBLE_QUOTE (DOT DOUBLE_QUOTE (column_spcl_char | BusinessString)+ DOUBLE_QUOTE)? (OPEN_PARENTHESES ((column_spcl_char | BusinessString)+) CLOSE_PARENTHESES)? ( OPEN_PARENTHESES ((PERIOD)? ( COMMA ((INT | INTNEG | BusinessString ))? COMMA ((INT | INTNEG | BusinessString ))? )?) CLOSE_PARENTHESES)? (AS DOUBLE_QUOTE BusinessString DOUBLE_QUOTE)?;

meColNameWithAlias	: ME_KW ( DOT DOUBLE_QUOTE  (column_spcl_char |  BusinessString )+ DOUBLE_QUOTE ( OPEN_PARENTHESES ((PERIOD)? ( COMMA ((INT | INTNEG | BusinessString ))? COMMA ((INT | INTNEG | BusinessString ))? )?) CLOSE_PARENTHESES)?)? (AS DOUBLE_QUOTE BusinessString DOUBLE_QUOTE)?;

meColName			: ME_KW ( DOT DOUBLE_QUOTE  ( column_spcl_char |  BusinessString )+ DOUBLE_QUOTE ( OPEN_PARENTHESES ((PERIOD)? ( COMMA ((INT | INTNEG | BusinessString ))? COMMA ((INT | INTNEG | BusinessString ))? )?) CLOSE_PARENTHESES)?)?;

column_spcl_char	: SINGLE_QUOTE | DIV | MINUS |INT | INTNEG |PLUS | OPEN_PARENTHESES | CLOSE_PARENTHESES | 'Y' | 'M' | 'D' ;

PERIOD              : PERIOD_KW OPEN_PARENTHESES BusinessString COMMA ((MINUS)? (INT|INTNEG)) (COMMA DATE|CurrentPeriod)? CLOSE_PARENTHESES;

CurrentPeriod   	: '$CurrentPeriod';

PERIOD_KW           : (Space)* P E R I O D (Space)*;

RELOPTS				: EQ_TO			
                    | NOT_EQ_TO		
                    | LESS_THAN		
                    | GTR_THAN		
                    | LESS_THAN_EQ_TO	
                    | GTR_THAN_EQ_TO;	
					
DATEPERIOD 			: (([0-9])([0-9])([0-9])([0-9]))((('0')[0-9])|(('1')[0-2]))(([0-2][0-9])|(('3')[0-1])) ;
DATEPERIODNEG 		: '-'(([0-9])([0-9])([0-9])([0-9]))((('0')[0-9])|(('1')[0-2]))(([0-2][0-9])|(('3')[0-1])) ;
DATE				: DOUBLE_QUOTE (([0-9])([0-9])([0-9])([0-9]))('-')((('0')[0-9])|(('1')[0-2]))('-')(([0-2][0-9])|(('3')[0-1])) DOUBLE_QUOTE;

OPEN_PARENTHESES	: (Space)* '(' (Space)*;
CLOSE_PARENTHESES	: (Space)* ')' (Space)*;
OPEN_BRACES			: (Space)* '{' (Space)*;
CLOSE_BRACES		: (Space)* '}' (Space)*;
OPEN_SQR_BRACKETS	: (Space)* '[' (Space)*;
CLOSE_SQR_BRACKETS	: (Space)* ']' (Space)*;

EQ_TO				: (Space)* '==' (Space)*;
NOT_EQ_TO			: (Space)* '!=' (Space)*;
LESS_THAN			: (Space)* '<' (Space)*;
GTR_THAN			: (Space)* '>' (Space)*;
LESS_THAN_EQ_TO		: (Space)* '<=' (Space)*;
GTR_THAN_EQ_TO		: (Space)* '>=' (Space)*;

PLUS				: (Space)* ('+') (Space)*;
MINUS				: (Space)* ('-') (Space)*;
MUL					: (Space)* ('*') (Space)*;
DIV					: (Space)* ('/') (Space)*;

BACKSLASH		: '\\';
                      
DOUBLE_QUOTE		: '"';
SINGLE_QUOTE		: '\'';
COLON				: (Space)* ':' (Space)*;
COMMA				: (Space)* ',' (Space)*;
DOT					: '.';
AS                  : (Space)* A S (Space)*;


INT					: (Space)*[0-9]+(Space)*;
//NUM					: [0-9];

DOUBLE				: [0-9]+DOT[0-9]+;
INTNEG				: '-'[0-9]+;
DOUBLENEG			: '-'[0-9]+DOT[0-9]+;
BusinessString		: [0-9a-zA-Z_&!#@$^\\ ]+;

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
	
Space		:	(' ' | '\t' | '\r' | '\n');

WS : [ \t\r\n]+ -> skip;

//nested_query : ( LPARAN nested_query RPARAN | ~( LPARAN | RPARAN ) )+;

//genericBlock
//  : OBR
//    ( ~(OBR | CBR)
//    | genericBlock
//    )*
//    CBR
//  ;