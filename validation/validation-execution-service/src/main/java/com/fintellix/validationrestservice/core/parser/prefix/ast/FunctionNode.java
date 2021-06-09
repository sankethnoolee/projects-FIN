package com.fintellix.validationrestservice.core.parser.prefix.ast;

import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;

public class FunctionNode extends AstNode {

    public FunctionNode(final Token token) {
        super(token);
        if (token.getType() != TokenType.FUNCTION && token.getType() != TokenType.ARTHEMATICFUNCTION) {
            throw new IllegalStateException("Token must be of type VARIABLE, found type: "
                    + token.getType() + ", value: " + token.getValue());
        }
    }

}
