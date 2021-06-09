var antlr4 = require('js/framework/validations/entities/antlrJavascript/antlr4/index');
var ValidationGrammarLexer = require('js/framework/validations/entities/antlrJavascript/parsers/expr/ValidationGrammarLexer');
var ValidationGrammarParser = require('js/framework/validations/entities/antlrJavascript/parsers/expr/ValidationGrammarParser');
$(document).ready(function() {
	/* $("#validationDescriptionTextArea").on("change keyup paste", function(e) {
	if($('#validationDescriptionTextArea').val().length>1){
		 var position = getCaretPosition($('#validationDescriptionTextArea')[0]);
		cursorPosY=17+position.y-$('#validationDescriptionTextArea')[0].getBoundingClientRect().top;
		cursorPosX=position.x-$('#validationDescriptionTextArea')[0].getBoundingClientRect().left;
		}
		if(event.keyCode == 190 || event.keyCode == 8){
			getColumnDetailsForAutoSuggestion(e)
		}
		
		callStringDescription(getInputForSuggestion()); 
		antlrValidation();
	}); */
});

function antlrValidation(){
	var input = $("#validationDescriptionTextArea").val();
	var chars = new antlr4.InputStream(input);
	var lexer = new ValidationGrammarLexer.ValidationGrammarLexer(chars);
	var tokens  = new antlr4.CommonTokenStream(lexer);
	var parser = new ValidationGrammarParser.ValidationGrammarParser(tokens);
	parser.buildParseTrees = true;
	var tree = parser.exp();
	if(tree.parser._syntaxErrors==0){
		$("#validation-msg-div").empty();
		$("#validation-msg-div").append("<small class='vs-baseline-regular-black'>Expression Valid.</small>").css('height','32px');
		$("#validation-msg").removeClass("error").addClass("success").css("display","flex");
		$('#validationDescriptionTextArea').addClass('textarea-height');
		//console.log(`parsed successful:\r\n${input}`);
	}
}