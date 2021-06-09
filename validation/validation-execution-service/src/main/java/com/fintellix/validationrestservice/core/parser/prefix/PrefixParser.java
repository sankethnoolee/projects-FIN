package com.fintellix.validationrestservice.core.parser.prefix;

import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenStream;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;
import com.fintellix.validationrestservice.core.parser.exception.ParserException;
import com.fintellix.validationrestservice.core.parser.prefix.ast.AstNode;
import com.fintellix.validationrestservice.core.parser.prefix.ast.FunctionNode;
import com.fintellix.validationrestservice.core.parser.prefix.ast.NumberNode;
import com.fintellix.validationrestservice.core.parser.prefix.ast.VariableNode;

import java.util.List;

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
                case BOOLEAN:
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
                    if (tokenStream.peek().getValue().equalsIgnoreCase("FOREACH")) {
                        if (node == null) {
                            return parseForEachFunctionCall(tokenStream);
                        }
                        node.getChildren().add(parseForEachFunctionCall(tokenStream));
                        return node;
                    } else {
                        if (node == null) {
                            return parseFunctionCall(tokenStream);
                        }
                        node.getChildren().add(parseFunctionCall(tokenStream));
                        return node;
                    }
                case LEFT_PAREN:
                    if (node == null) {
                        return parsePriorityCall(tokenStream);
                    }
                    node.getChildren().add(parsePriorityCall(tokenStream));
                    return node;
                case RIGHT_PAREN:
                    return node;
                case LEFT_FLOWER_PAREN:
                    return node;
                case RIGHT_FLOWER_PAREN:
                    return node;
                case ARTHEMATICFUNCTION:
                    if (node == null) {
                        return new VariableNode(tokenStream.next());
                    }
                    node.getChildren().add(new VariableNode(tokenStream.next()));
                    return node;
                default:
                    continue;
            }
        }
        return node;
    }

    private FunctionNode parseFunctionCall(final TokenStream tokenStream) {
        final Token functionName = tokenStream.next();
        final FunctionNode functionNode = new FunctionNode(functionName);
        Integer max = 1;
        if (tokenStream.peek().getType() != TokenType.LEFT_PAREN) {
            throw new ParserException("Functions must be followed by a '(', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume '('

        do {
            max++;
            parseExpression(tokenStream, functionNode);
        } while (tokenStream.peek().getType() != TokenType.RIGHT_PAREN && tokenStream.getSize() >= max);

        if (tokenStream.peek().getType() != TokenType.RIGHT_PAREN) {
            throw new ParserException("Functions must be closed with a ')', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume ')'

        return functionNode;
    }

    private FunctionNode parsePriorityCall(final TokenStream tokenStream) {
        final FunctionNode functionNode = new FunctionNode(new Token("PRIORITYBRACKETS", TokenType.FUNCTION));
        Integer max = 1;
        if (tokenStream.peek().getType() != TokenType.LEFT_PAREN) {
            throw new ParserException("Functions must be followed by a '(', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume '('

        do {
            max++;
            parseExpression(tokenStream, functionNode);
        } while (tokenStream.peek().getType() != TokenType.RIGHT_PAREN && tokenStream.getSize() >= max);

        if (tokenStream.peek().getType() != TokenType.RIGHT_PAREN) {
            throw new ParserException("Functions must be closed with a ')', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume ')'

        return functionNode;
    }

    private FunctionNode parseForEachFunctionCall(final TokenStream tokenStream) {
        final Token functionName = tokenStream.next();
        final FunctionNode functionNode = new FunctionNode(functionName);
        Integer max = 1;
        if (tokenStream.peek().getType() != TokenType.LEFT_PAREN) {
            throw new ParserException("Functions must be followed by a '(', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume '('

        do {
            max++;
            parseExpression(tokenStream, functionNode);
        } while (tokenStream.peek().getType() != TokenType.RIGHT_PAREN && tokenStream.getSize() >= max);

        if (tokenStream.peek().getType() != TokenType.RIGHT_PAREN) {
            throw new ParserException("Functions must be closed with a ')', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume ')'

        if (tokenStream.peek().getType() != TokenType.LEFT_FLOWER_PAREN) {
            throw new ParserException("Functions must be followed by a '(', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume '('

        do {
            max++;
            parseExpression(tokenStream, functionNode);
        } while (tokenStream.peek().getType() != TokenType.RIGHT_FLOWER_PAREN && tokenStream.getSize() >= max);

        if (tokenStream.peek().getType() != TokenType.RIGHT_FLOWER_PAREN) {
            throw new ParserException("Functions must be closed with a ')', found: " + tokenStream.peek().getValue());
        }
        tokenStream.next(); // consume ')'

        return functionNode;
    }

}
