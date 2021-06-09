/* lexical grammar */

%lex

%options case-insensitive
	
%%

\s+                 		/* skip whitespace */
"SUBSTR"		           	return 'SUBSTR'
"LOWER"		           		return 'LOWER'
"UPPER"		           		return 'UPPER'
"LEN"		           		return 'LEN'
"RTN"		           		return 'RTN'
"REFTBL"		           	return 'REFTBL'
'"'							return 'DOUBLEQUOTE'
"."		           			return 'DOT'
"("							return 'OP1'
")"							return 'CP1'
"_"							return 'UNDERSCORE'
","                     	return 'COMMA'
[0-9]+              	 	return 'INT'
[A-Za-z_]+	           	return 'STRING'
<<EOF>>               		return 'EOF'

/lex
/* enable EBNF grammar syntax */
%ebnf
%start startrule

%% /* language grammar */

startrule					: start_rule EOF;

start_rule					:sum_expr|lower_expr|upper_expr|len_expr;

sum_expr					:'SUBSTR'	'OP1'	(rtn_expr|reftbl_expr|ddentity_expr|start_rule)	'COMMA'	exp_num	'COMMA' exp_num		'CP1';	
lower_expr					:'LOWER'	'OP1'	(rtn_expr|reftbl_expr|ddentity_expr|start_rule)										'CP1';	
upper_expr					:'UPPER'	'OP1'	(rtn_expr|reftbl_expr|ddentity_expr|start_rule)										'CP1';	
len_expr					:'LEN'		'OP1'	(rtn_expr|reftbl_expr|ddentity_expr|start_rule)										'CP1';							

rtn_expr					:'RTN' 'DOT' 'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE' 
							|'RTN' 'DOT' 'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE' 'DOT'	'DOUBLEQUOTE' alphanumeric 'DOUBLEQUOTE'
							|'RTN' 'DOT' 'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE' 'DOT'	'DOUBLEQUOTE' alphanumeric 'DOUBLEQUOTE' 'DOT'	'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE'
							|'RTN' 'DOT' 'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE' 'DOT'	'DOUBLEQUOTE' alphanumeric 'DOUBLEQUOTE' 'DOT'	'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE' 'DOT'	'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE';
							
reftbl_expr					:'REFTBL'	'DOT'	'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE'
							|'REFTBL'	'DOT'	'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE'	'DOT'	'DOUBLEQUOTE' alphanumeric 'DOUBLEQUOTE'
							|'REFTBL'	'DOT'	'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE'	'DOT'	'DOUBLEQUOTE'	alphanumeric 'DOT'	alphanumeric	'DOUBLEQUOTE';

ddentity_expr				:'ENT'	'DOT'	'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE'
							|'ENT'	'DOT'	'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE'	'DOT'	'DOUBLEQUOTE' alphanumeric 'DOUBLEQUOTE'
							|'ENT'	'DOT'	'DOUBLEQUOTE' alphanumeric	'DOUBLEQUOTE'	'DOT'	'DOUBLEQUOTE'	alphanumeric 'DOT'	alphanumeric	'DOUBLEQUOTE';
														
exp_num						:'INT'|'INT' 'DOT' 'INT';



alphanumeric				:('INT'|'STRING'|"_")+;
							