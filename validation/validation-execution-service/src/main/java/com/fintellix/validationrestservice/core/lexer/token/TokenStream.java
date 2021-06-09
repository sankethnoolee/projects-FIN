package com.fintellix.validationrestservice.core.lexer.token;

import com.fintellix.validationrestservice.core.lexer.exception.EndOfStreamException;

import java.util.ArrayList;
import java.util.List;

public class TokenStream {

    private final List<Token> tokens = new ArrayList<>();

    private int currentToken;

    public TokenStream(final List<Token> tokens) {
        this.tokens.addAll(tokens);
        currentToken = 0;
    }

    public Token next() {
        final Token next = peek();
        currentToken++;
        return next;
    }

    public Token peek() {
        if (currentToken == tokens.size()) {
            throw new EndOfStreamException();
        }
        return tokens.get(currentToken);
    }

    public boolean hasNext() {
        return currentToken < tokens.size();
    }

    public int getPosition() {
        return currentToken;
    }

    public void reset() {
        currentToken = 0;
    }

    public Integer getSize() {
        return tokens.size();
    }

}
