package com.sanketh.noolee.lexer.tokenizer;

import com.sanketh.noolee.lexer.exception.LexerException;
import com.sanketh.noolee.lexer.token.Token;
import com.sanketh.noolee.lexer.token.TokenType;

import java.util.*;

/**
 * Created by kenny on 2/29/16.
 */
public class ExpressionTokenizer {
    private static final Set<String> FUNCTIONS = new HashSet<>(Arrays.asList("sin", "cos", "add", "sub", "mul", "div"));

    private final NumberTokenizer numberTokenizer = new NumberTokenizer();

    public List<Token> tokenize(final CharacterStream tokenStream) {
        final List<Token> tokens = new ArrayList<>();

        while (tokenStream.hasNext()) {
            final char token = Character.toLowerCase(tokenStream.peek());

            if (token == '(') {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.LEFT_PAREN));
            }
            else if(token == ')') {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.RIGHT_PAREN));
            }
            else if (Character.isDigit(token) || token == '.') {
                tokens.add(numberTokenizer.tokenize(tokenStream));
            }
            else if (Character.isAlphabetic(token)) {
                tokens.add(consumeString(tokenStream));
            }
            else if (token == ' ') {
                tokenStream.next(); // ignore token
            }
            else {
                throw new LexerException("Found unrecognized character [" + token + "]");
            }
        }

        return tokens;
    }

    private Token consumeString(final CharacterStream tokenStream) {
        final StringBuilder stringBuilder = new StringBuilder();

        while (tokenStream.hasNext()) {
            if (Character.isAlphabetic(tokenStream.peek())) {
                stringBuilder.append(tokenStream.next());

            } else {
                break;
            }
        }
        final String token = stringBuilder.toString();

        if (FUNCTIONS.contains(token)) {
            return new Token(token, TokenType.FUNCTION);
        }
        return new Token(token, TokenType.VARIABLE);
    }

}
