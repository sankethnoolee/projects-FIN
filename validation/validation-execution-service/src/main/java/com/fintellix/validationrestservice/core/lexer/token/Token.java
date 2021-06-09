package com.fintellix.validationrestservice.core.lexer.token;

public class Token {
    private final TokenType type;
    private String value;

    public Token(final String value, final TokenType type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }
}
