//
/* Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

// Provides an empty default implementation of {@link ANTLRErrorListener}. The
// default implementation of each method does nothing, but can be overridden as
// necessary.

function ErrorListener() {
	return this;
}

ErrorListener.prototype.syntaxError = function(recognizer, offendingSymbol, line, column, msg, e) {
};

ErrorListener.prototype.reportAmbiguity = function(recognizer, dfa, startIndex, stopIndex, exact, ambigAlts, configs) {
};

ErrorListener.prototype.reportAttemptingFullContext = function(recognizer, dfa, startIndex, stopIndex, conflictingAlts, configs) {
};

ErrorListener.prototype.reportContextSensitivity = function(recognizer, dfa, startIndex, stopIndex, prediction, configs) {
};

function ConsoleErrorListener() {
	ErrorListener.call(this);
	return this;
}

ConsoleErrorListener.prototype = Object.create(ErrorListener.prototype);
ConsoleErrorListener.prototype.constructor = ConsoleErrorListener;

//
// Provides a default instance of {@link ConsoleErrorListener}.
//
ConsoleErrorListener.INSTANCE = new ConsoleErrorListener();

//
// {@inheritDoc}
//
// <p>
// This implementation prints messages to {@link System//err} containing the
// values of {@code line}, {@code charPositionInLine}, and {@code msg} using
// the following format.</p>
//
// <pre>
// line <em>line</em>:<em>charPositionInLine</em> <em>msg</em>
// </pre>
//
ConsoleErrorListener.prototype.syntaxError = function(recognizer, offendingSymbol, line, column, msg, e) {
	if($("#validationDescriptionTextArea").val()!=""){
		$("#validation-msg-div").empty();
		$("#validation-msg-div").append("<small class='vs-baseline-regular-black'>Syntax Error at line "+ line +"</small>");
		
				var childElement = document.createElement('div');
                childElement.setAttribute('class', 'vs-tooltip icon-info');
				childElement.setAttribute('style', 'background:transparent;border:none;');
                var iElement = document.createElement('i');
                iElement.setAttribute('class', 'icon-small icon-info-solid');
                var spanElement = document.createElement('span');
                spanElement.setAttribute('class', 'vs-tooltiptext-right');
                spanElement.setAttribute('style', 'width:550px;z-index:9999');
                var smallElement = document.createElement('small');
                smallElement.setAttribute('class', 'vs-baseline-regular-black error');
                smallElement.textContent = msg;
                var innerSpanElement = document.createElement('span');
                innerSpanElement.setAttribute('class', 'vs-baseline-medium-primary');
                //innerSpanElement.textContent = val.entityName;
                var br = document.createElement('br');
                smallElement.appendChild(innerSpanElement);
                spanElement.appendChild(smallElement).appendChild(br);
				childElement.appendChild(iElement);
                childElement.appendChild(spanElement);
				$("#validation-msg-div").append(childElement);
		
		
		
		$("#validation-msg").removeClass("success").addClass("error").css("display","flex");
		$('#validationDescriptionTextArea').addClass('textarea-height');
		var position = getCaretPosition($('#validationDescriptionTextArea')[0]);
		cursorPosY=17+position.y-$('#validationDescriptionTextArea')[0].getBoundingClientRect().top;
		cursorPosX=position.x-$('#validationDescriptionTextArea')[0].getBoundingClientRect().left;
		//console.error("line " + line + ":" + column + " " + msg);
	}else{
		$("#validation-msg-div").empty();
		$("#validation-msg").css("display","none");
		$('#validationDescriptionTextArea').removeClass('textarea-height');
	}
};

function ProxyErrorListener(delegates) {
	ErrorListener.call(this);
    if (delegates===null) {
        throw "delegates";
    }
    this.delegates = delegates;
	return this;
}

ProxyErrorListener.prototype = Object.create(ErrorListener.prototype);
ProxyErrorListener.prototype.constructor = ProxyErrorListener;

ProxyErrorListener.prototype.syntaxError = function(recognizer, offendingSymbol, line, column, msg, e) {
    this.delegates.map(function(d) { d.syntaxError(recognizer, offendingSymbol, line, column, msg, e); });
};

ProxyErrorListener.prototype.reportAmbiguity = function(recognizer, dfa, startIndex, stopIndex, exact, ambigAlts, configs) {
    this.delegates.map(function(d) { d.reportAmbiguity(recognizer, dfa, startIndex, stopIndex, exact, ambigAlts, configs); });
};

ProxyErrorListener.prototype.reportAttemptingFullContext = function(recognizer, dfa, startIndex, stopIndex, conflictingAlts, configs) {
	this.delegates.map(function(d) { d.reportAttemptingFullContext(recognizer, dfa, startIndex, stopIndex, conflictingAlts, configs); });
};

ProxyErrorListener.prototype.reportContextSensitivity = function(recognizer, dfa, startIndex, stopIndex, prediction, configs) {
	this.delegates.map(function(d) { d.reportContextSensitivity(recognizer, dfa, startIndex, stopIndex, prediction, configs); });
};

exports.ErrorListener = ErrorListener;
exports.ConsoleErrorListener = ConsoleErrorListener;
exports.ProxyErrorListener = ProxyErrorListener;

