package com.fintellix.validationrestservice.core.parser.prefix.ast;

import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;

public class BracketNode extends AstNode {

    public BracketNode(final Token token) {
        super(token);
        if (token.getType() != TokenType.LEFT_PAREN) {
            throw new IllegalStateException("Token must be of type LEFT_PAREN, found type: "
                    + token.getType() + ", value: " + token.getValue());
        }
    }

}
