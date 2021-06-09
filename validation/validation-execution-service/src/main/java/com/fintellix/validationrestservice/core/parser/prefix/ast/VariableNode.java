package com.fintellix.validationrestservice.core.parser.prefix.ast;

import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;

public class VariableNode extends AstNode {

    public VariableNode(final Token token) {
        super(token);
        if ((token.getType() != TokenType.VARIABLE) && (token.getType() != TokenType.ARTHEMATICFUNCTION)) {
            throw new IllegalStateException("Token must be of type VARIABLE, found type: "
                    + token.getType() + ", value: " + token.getValue());
        }
    }

}
