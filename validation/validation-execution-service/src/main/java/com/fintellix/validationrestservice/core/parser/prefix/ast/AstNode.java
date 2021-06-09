package com.fintellix.validationrestservice.core.parser.prefix.ast;

import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.definition.ExpressionResultDetail;

import java.util.ArrayList;
import java.util.List;

public class AstNode {
    private final Token token;
    private ExpressionResultDetail expressionDetails;
    private List<AstNode> children = new ArrayList<>();

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

    public ExpressionResultDetail getExpressionDetails() {
        return expressionDetails;
    }

    public void setExpressionDetails(ExpressionResultDetail expressionDetails) {
        this.expressionDetails = expressionDetails;
    }
}
