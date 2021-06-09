package com.kennycason.soroban.lexer;

import com.kennycason.soroban.lexer.token.Token;
import com.kennycason.soroban.lexer.token.TokenType;
import com.kennycason.soroban.lexer.tokenizer.CharacterStream;
import com.kennycason.soroban.lexer.tokenizer.ExpressionTokenizer;
import org.junit.Test;

/**
 * Created by kenny on 3/1/16.
 */
public class ExpressionTokenizerTest {

    private final ExpressionTokenizer expressionTokenizer = new ExpressionTokenizer();

    @Test
    public void basic() {
        TokenTestUtils.assertValid(expressionTokenizer.tokenize(new CharacterStream("add(x y)")),
                new Token("add", TokenType.FUNCTION),
                new Token("(", TokenType.LEFT_PAREN),
                new Token("x", TokenType.VARIABLE),
                new Token("y", TokenType.VARIABLE),
                new Token(")", TokenType.RIGHT_PAREN)
        );

        TokenTestUtils.assertValid(expressionTokenizer.tokenize(new CharacterStream("sin(10)")),
                new Token("sin", TokenType.FUNCTION),
                new Token("(", TokenType.LEFT_PAREN),
                new Token("10", TokenType.NUMBER),
                new Token(")", TokenType.RIGHT_PAREN)
        );

        TokenTestUtils.assertValid(expressionTokenizer.tokenize(new CharacterStream("add(sin(x) cos(y))")),
                new Token("add", TokenType.FUNCTION),
                new Token("(", TokenType.LEFT_PAREN),
                new Token("sin", TokenType.FUNCTION),
                new Token("(", TokenType.LEFT_PAREN),
                new Token("x", TokenType.VARIABLE),
                new Token(")", TokenType.RIGHT_PAREN),
                new Token("cos", TokenType.FUNCTION),
                new Token("(", TokenType.LEFT_PAREN),
                new Token("y", TokenType.VARIABLE),
                new Token(")", TokenType.RIGHT_PAREN),
                new Token(")", TokenType.RIGHT_PAREN)
        );

    }

}
