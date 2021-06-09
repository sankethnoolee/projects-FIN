package com.fintellix.validationrestservice.core.parser.prefix.ast;

import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;

public class PriorityNode extends AstNode {

    public PriorityNode(final Token token) {
        super(token);
        if (token.getType() != TokenType.LEFT_PAREN) {
            throw new IllegalStateException("Token must be of type VARIABLE, found type: "
                    + token.getType() + ", value: " + token.getValue());
        }
    }

}
