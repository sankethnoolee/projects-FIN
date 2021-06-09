package com.fintellix.validationrestservice.core.parser.prefix.ast;

import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;

public class NumberNode extends AstNode {

    public NumberNode(final Token token) {
        super(token);

        if (token.getType() != TokenType.NUMBER) {
            throw new IllegalStateException("Token must be of type NUMBER, found type: "
                    + token.getType() + ", value: " + token.getValue());
        }
    }

}
