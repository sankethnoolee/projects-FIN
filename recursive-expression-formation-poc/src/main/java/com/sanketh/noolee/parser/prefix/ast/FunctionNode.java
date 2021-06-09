package com.sanketh.noolee.parser.prefix.ast;

import com.sanketh.noolee.lexer.token.Token;
import com.sanketh.noolee.lexer.token.TokenType;

/**
 * Created by kenny on 3/1/16.
 */
public class FunctionNode extends AstNode {

    public FunctionNode(final Token token) {
        super(token);
        if (token.getType() != TokenType.FUNCTION) {
            throw new IllegalStateException("Token must be of type VARIABLE, found type: "
                         + token.getType() + ", value: " + token.getValue());
        }
    }

}
