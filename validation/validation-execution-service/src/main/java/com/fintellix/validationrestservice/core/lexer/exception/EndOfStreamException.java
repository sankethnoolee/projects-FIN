package com.fintellix.validationrestservice.core.lexer.exception;

public class EndOfStreamException extends LexerException {
    public EndOfStreamException() {
        super("No tokens remain in stream");
    }
}
