package com.sanketh.noolee.parser.prefix.ast;

import com.sanketh.noolee.lexer.token.Token;
import com.sanketh.noolee.lexer.token.TokenType;

/**
 * Created by kenny on 3/1/16.
 */
public class VariableNode extends AstNode {

    public VariableNode(final Token token) {
        super(token);
        if (token.getType() != TokenType.VARIABLE) {
            throw new IllegalStateException("Token must be of type VARIABLE, found type: "
                         + token.getType() + ", value: " + token.getValue());
        }
    }

}
