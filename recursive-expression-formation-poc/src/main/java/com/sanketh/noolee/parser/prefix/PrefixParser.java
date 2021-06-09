package com.sanketh.noolee.parser.prefix;

import com.sanketh.noolee.lexer.token.Token;
import com.sanketh.noolee.lexer.token.TokenStream;
import com.sanketh.noolee.lexer.token.TokenType;
import com.sanketh.noolee.parser.exception.ParserException;
import com.sanketh.noolee.parser.prefix.ast.AstNode;
import com.sanketh.noolee.parser.prefix.ast.FunctionNode;
import com.sanketh.noolee.parser.prefix.ast.NumberNode;
import com.sanketh.noolee.parser.prefix.ast.VariableNode;

import java.util.List;

/**
 * Created by kenny on 3/1/16.
 *
 * build an AST for a pure prefix language
 *
 * e.g.
 * sin(x)
 * sin(cos(add(x y))))
 * sin(cos(add(2 neg(10))))
 */
public class PrefixParser {

    public AstNode parse(final List<Token> tokens) {
        return parseExpression(new TokenStream(tokens), null);
    }

    private AstNode parseExpression(final TokenStream tokenStream, final AstNode node) {
        while (tokenStream.hasNext()) {
            switch (tokenStream.peek().getType()) {
                case VARIABLE:
                    if (node == null) {
                        return new VariableNode(tokenStream.next());
                    }
                    node.getChildren().add(new VariableNode(tokenStream.next()));
                    return node;

                case NUMBER:
                    if (node == null) {
                        return new NumberNode(tokenStream.next());
                    }
                    node.getChildren().add(new NumberNode(tokenStream.next()));
                    return node;

                case FUNCTION:
                    if (node == null) {
                        return parseFunctionCall(tokenStream);
                    }
                    node.getChildren().add(parseFunctionCall(tokenStream));
                    return node;

                case LEFT_PAREN:
                case RIGHT_PAREN:
                    return node;
            }
        }
        return node;
    }

    private FunctionNode parseFunctionCall(final TokenStream tokenStream) {
        final Token functionName = tokenStream.next();
        final FunctionNode functionNode = new FunctionNode(functionName);

        if (tokenStream.peek().getType() != TokenType.LEFT_PAREN) {
            throw new ParserException("Functions must be followed by a '(', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume '('

        do {
            parseExpression(tokenStream, functionNode);
        } while (tokenStream.peek().getType() != TokenType.RIGHT_PAREN);

        if (tokenStream.peek().getType() != TokenType.RIGHT_PAREN) {
            throw new ParserException("Functions must be closed with a ')', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume ')'

        return functionNode;
    }


}
