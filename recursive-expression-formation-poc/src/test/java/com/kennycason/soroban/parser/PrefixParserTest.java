package com.kennycason.soroban.parser;

import com.kennycason.soroban.lexer.tokenizer.CharacterStream;
import com.kennycason.soroban.lexer.tokenizer.ExpressionTokenizer;
import com.kennycason.soroban.parser.prefix.ast.AstNode;
import com.kennycason.soroban.parser.prefix.PrefixParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by kenny on 3/2/16.
 */
public class PrefixParserTest {
    private final ExpressionTokenizer expressionTokenizer = new ExpressionTokenizer();
    private final PrefixParser prefixParser = new PrefixParser();

    @Test
    public void prefixFunctions() {
        final AstNode root = parse("sin(10)");
        assertEquals("sin", root.getToken().getValue());
        assertEquals(1, root.getChildren().size());
        assertEquals("10", root.getChildren().get(0).getToken().getValue());

        final AstNode root2 = parse("cos(x)");
        assertEquals("cos", root2.getToken().getValue());
        assertEquals(1, root2.getChildren().size());
        assertEquals("x", root2.getChildren().get(0).getToken().getValue());

        final AstNode root3 = parse("cos(sin(x))");
        assertEquals("cos", root3.getToken().getValue());
        assertEquals(1, root3.getChildren().size());
        assertEquals("sin", root3.getChildren().get(0).getToken().getValue());
        assertEquals(1, root3.getChildren().get(0).getChildren().size());
        assertEquals("x", root3.getChildren().get(0).getChildren().get(0).getToken().getValue());

        final AstNode root4 = parse("add(10 20)");
        assertEquals("add", root4.getToken().getValue());
        assertEquals(2, root4.getChildren().size());
        assertEquals("10", root4.getChildren().get(0).getToken().getValue());
        assertEquals("20", root4.getChildren().get(1).getToken().getValue());
    }

    @Test
    public void nestedTest() {
        final AstNode root5 = parse("add(sin(10) cos(20))");
        assertEquals("add", root5.getToken().getValue());
        assertEquals(2, root5.getChildren().size());

        assertEquals("sin", root5.getChildren().get(0).getToken().getValue());
        assertEquals(1, root5.getChildren().get(0).getChildren().size());
        assertEquals("10", root5.getChildren().get(0).getChildren().get(0).getToken().getValue());

        assertEquals("cos", root5.getChildren().get(1).getToken().getValue());
        assertEquals(1, root5.getChildren().get(1).getChildren().size());
        assertEquals("20", root5.getChildren().get(1).getChildren().get(0).getToken().getValue());
    }

    private AstNode parse(final String expr) {
        return  prefixParser.parse(
                        expressionTokenizer.tokenize(new CharacterStream(expr)));
    }
}
