package com.sanketh.noolee.parser.prefix.ast;

import com.sanketh.noolee.lexer.token.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kenny on 3/2/16.
 */
public class AstNode {
    private final Token token;

    private final List<AstNode> children = new ArrayList<>();

    public AstNode(final Token token, final AstNode... children) {
        this.token = token;
        for (AstNode child : children) {
            this.children.add(child);
        }
    }

    public Token getToken() {
        return token;
    }

    public List<AstNode> getChildren() {
        return children;
    }
}
