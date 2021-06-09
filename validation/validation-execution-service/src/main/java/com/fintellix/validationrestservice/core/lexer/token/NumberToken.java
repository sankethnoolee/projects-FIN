package com.fintellix.validationrestservice.core.lexer.token;

public class NumberToken extends Token {
    private final Base base;

    public NumberToken(final String value, final Base base) {
        super(value, TokenType.NUMBER);
        this.base = base;
    }

    public Base getBase() {
        return base;
    }

    public enum Base {
        BINARY,
        TEN,
        HEXADECIMAL
    }

}
