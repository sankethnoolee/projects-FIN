package com.fintellix.validationrestservice.core.lexer.tokenizer;

import com.fintellix.validationrestservice.core.lexer.exception.EndOfStreamException;

public class CharacterStream {

    private final String text;

    private int currentToken;

    public CharacterStream(final String text) {
        this.text = text;
        currentToken = 0;
    }

    public char next() {
        final char next = peek();
        currentToken++;
        return next;
    }

    public char peek() {
        if (currentToken == text.length()) {
            throw new EndOfStreamException();
        }
        return text.charAt(currentToken);
    }

    public boolean hasNext() {
        return currentToken < text.length();
    }

    public int getPosition() {
        return currentToken;
    }

    public void reset() {
        currentToken = 0;
    }

}
