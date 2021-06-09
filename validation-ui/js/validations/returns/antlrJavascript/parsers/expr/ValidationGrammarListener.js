// Generated from ValidationGrammar.g4 by ANTLR 4.7
// jshint ignore: start
var antlr4 = require('js/framework/validations/returns/antlrJavascript/antlr4/index');

// This class defines a complete listener for a parse tree produced by ValidationGrammarParser.
function ValidationGrammarListener() {
	antlr4.tree.ParseTreeListener.call(this);
	return this;
}

ValidationGrammarListener.prototype = Object.create(antlr4.tree.ParseTreeListener.prototype);
ValidationGrammarListener.prototype.constructor = ValidationGrammarListener;

// Enter a parse tree produced by ValidationGrammarParser#r.
ValidationGrammarListener.prototype.enterR = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#r.
ValidationGrammarListener.prototype.exitR = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#exp.
ValidationGrammarListener.prototype.enterExp = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#exp.
ValidationGrammarListener.prototype.exitExp = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#dateFunction.
ValidationGrammarListener.prototype.enterDateFunction = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#dateFunction.
ValidationGrammarListener.prototype.exitDateFunction = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#numericFunction.
ValidationGrammarListener.prototype.enterNumericFunction = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#numericFunction.
ValidationGrammarListener.prototype.exitNumericFunction = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#stringFunction.
ValidationGrammarListener.prototype.enterStringFunction = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#stringFunction.
ValidationGrammarListener.prototype.exitStringFunction = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#ifStatement.
ValidationGrammarListener.prototype.enterIfStatement = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#ifStatement.
ValidationGrammarListener.prototype.exitIfStatement = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#sumifStatement.
ValidationGrammarListener.prototype.enterSumifStatement = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#sumifStatement.
ValidationGrammarListener.prototype.exitSumifStatement = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#maxifStatement.
ValidationGrammarListener.prototype.enterMaxifStatement = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#maxifStatement.
ValidationGrammarListener.prototype.exitMaxifStatement = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#minifStatement.
ValidationGrammarListener.prototype.enterMinifStatement = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#minifStatement.
ValidationGrammarListener.prototype.exitMinifStatement = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#countifStatment.
ValidationGrammarListener.prototype.enterCountifStatment = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#countifStatment.
ValidationGrammarListener.prototype.exitCountifStatment = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#betweenStatment.
ValidationGrammarListener.prototype.enterBetweenStatment = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#betweenStatment.
ValidationGrammarListener.prototype.exitBetweenStatment = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#logicalExp.
ValidationGrammarListener.prototype.enterLogicalExp = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#logicalExp.
ValidationGrammarListener.prototype.exitLogicalExp = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#len.
ValidationGrammarListener.prototype.enterLen = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#len.
ValidationGrammarListener.prototype.exitLen = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#lookup.
ValidationGrammarListener.prototype.enterLookup = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#lookup.
ValidationGrammarListener.prototype.exitLookup = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#forEach.
ValidationGrammarListener.prototype.enterForEach = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#forEach.
ValidationGrammarListener.prototype.exitForEach = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#and.
ValidationGrammarListener.prototype.enterAnd = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#and.
ValidationGrammarListener.prototype.exitAnd = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#or.
ValidationGrammarListener.prototype.enterOr = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#or.
ValidationGrammarListener.prototype.exitOr = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#not.
ValidationGrammarListener.prototype.enterNot = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#not.
ValidationGrammarListener.prototype.exitNot = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#notIn.
ValidationGrammarListener.prototype.enterNotIn = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#notIn.
ValidationGrammarListener.prototype.exitNotIn = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#in.
ValidationGrammarListener.prototype.enterIn = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#in.
ValidationGrammarListener.prototype.exitIn = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#contains.
ValidationGrammarListener.prototype.enterContains = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#contains.
ValidationGrammarListener.prototype.exitContains = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#begins.
ValidationGrammarListener.prototype.enterBegins = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#begins.
ValidationGrammarListener.prototype.exitBegins = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#ends.
ValidationGrammarListener.prototype.enterEnds = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#ends.
ValidationGrammarListener.prototype.exitEnds = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#isempty.
ValidationGrammarListener.prototype.enterIsempty = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#isempty.
ValidationGrammarListener.prototype.exitIsempty = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#isnotempty.
ValidationGrammarListener.prototype.enterIsnotempty = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#isnotempty.
ValidationGrammarListener.prototype.exitIsnotempty = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#unique.
ValidationGrammarListener.prototype.enterUnique = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#unique.
ValidationGrammarListener.prototype.exitUnique = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#sum.
ValidationGrammarListener.prototype.enterSum = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#sum.
ValidationGrammarListener.prototype.exitSum = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#max.
ValidationGrammarListener.prototype.enterMax = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#max.
ValidationGrammarListener.prototype.exitMax = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#min.
ValidationGrammarListener.prototype.enterMin = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#min.
ValidationGrammarListener.prototype.exitMin = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#count.
ValidationGrammarListener.prototype.enterCount = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#count.
ValidationGrammarListener.prototype.exitCount = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#dcount.
ValidationGrammarListener.prototype.enterDcount = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#dcount.
ValidationGrammarListener.prototype.exitDcount = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#avg.
ValidationGrammarListener.prototype.enterAvg = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#avg.
ValidationGrammarListener.prototype.exitAvg = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#round.
ValidationGrammarListener.prototype.enterRound = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#round.
ValidationGrammarListener.prototype.exitRound = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#abs.
ValidationGrammarListener.prototype.enterAbs = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#abs.
ValidationGrammarListener.prototype.exitAbs = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#percentage.
ValidationGrammarListener.prototype.enterPercentage = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#percentage.
ValidationGrammarListener.prototype.exitPercentage = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#stringLiterals.
ValidationGrammarListener.prototype.enterStringLiterals = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#stringLiterals.
ValidationGrammarListener.prototype.exitStringLiterals = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#rtnColName.
ValidationGrammarListener.prototype.enterRtnColName = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#rtnColName.
ValidationGrammarListener.prototype.exitRtnColName = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#rtnColNameWithAlias.
ValidationGrammarListener.prototype.enterRtnColNameWithAlias = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#rtnColNameWithAlias.
ValidationGrammarListener.prototype.exitRtnColNameWithAlias = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#refColName.
ValidationGrammarListener.prototype.enterRefColName = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#refColName.
ValidationGrammarListener.prototype.exitRefColName = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#refColNameWithAlias.
ValidationGrammarListener.prototype.enterRefColNameWithAlias = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#refColNameWithAlias.
ValidationGrammarListener.prototype.exitRefColNameWithAlias = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#ddEntityColName.
ValidationGrammarListener.prototype.enterDdEntityColName = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#ddEntityColName.
ValidationGrammarListener.prototype.exitDdEntityColName = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#ddEntityColNameWithAlias.
ValidationGrammarListener.prototype.enterDdEntityColNameWithAlias = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#ddEntityColNameWithAlias.
ValidationGrammarListener.prototype.exitDdEntityColNameWithAlias = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#meColNameWithAlias.
ValidationGrammarListener.prototype.enterMeColNameWithAlias = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#meColNameWithAlias.
ValidationGrammarListener.prototype.exitMeColNameWithAlias = function(ctx) {
};


// Enter a parse tree produced by ValidationGrammarParser#meColName.
ValidationGrammarListener.prototype.enterMeColName = function(ctx) {
};

// Exit a parse tree produced by ValidationGrammarParser#meColName.
ValidationGrammarListener.prototype.exitMeColName = function(ctx) {
};



exports.ValidationGrammarListener = ValidationGrammarListener;