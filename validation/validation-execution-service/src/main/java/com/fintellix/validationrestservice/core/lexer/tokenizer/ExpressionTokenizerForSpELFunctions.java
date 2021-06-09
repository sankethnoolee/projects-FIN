package com.fintellix.validationrestservice.core.lexer.tokenizer;

import com.fintellix.validationrestservice.core.lexer.exception.LexerException;
import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;
import com.fintellix.validationrestservice.util.ValidationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpressionTokenizerForSpELFunctions {

    private static final Set<String> FUNCTIONS = new HashSet<>(Arrays.asList("FOREACH", "IF", "PERIOD", "VLOOKUP",
            "SUMIF", "STRING", "EQ", "NEQ", "FILTER", "MAXIF", "MINIF", "COUNTIF", "SUM", "MAX", "MIN", "COUNT",
            "OFFSET", "YEARIN", "LEN", "LOWER", "UPPER", "SUBSTR", "THEN", "ELSE", "BETWEEN", "ISEMPTY", "ISNOTEMPTY",
            "IN", "NOTIN","AND", "OR","ABS", "CONCAT", "ROUND", "AVG", "CONVERT", "REGEX", "BETWEEN", "BEGINSWITH",
            "ENDSWITH", "CONTAINS", "TODATE", "SOM", "EOM", "SOY", "EOY", "SOFY", "EOFY", "DATEPART", "DATEDIFF",
            "DCOUNT", "UNIQUE"));
    //+ is tempm added here try with seprate queue
    private static final Set<String> BOOLEAN = new HashSet<>(Arrays.asList("FALSE", "TRUE"));
    private static final Set<String> ART_LOG_FUNC = new HashSet<>(Arrays.asList("+", "-", "/", "*", ">", "<", "=", "!", "?", ":"));
    private static final Set<String> SPECIAL_CHAR = new HashSet<>(
            Arrays.asList(".", "`", "~", "!", "@", "#", "$", "%", "^", "&", ";", "<", ">", "\\", "|", "\"", "'", "_", "£"));

    static {
        SPECIAL_CHAR.addAll(Arrays.stream(ValidationProperties.getValue("app.validation.specialTokensCSV").split(","))
                .map(String::trim)
                .collect(Collectors.toSet()));
    }

    private final NumberTokenizer numberTokenizer = new NumberTokenizer();
    private final LogicalOperatorsTokenizer logicalOperatorsTokenizer = new LogicalOperatorsTokenizer();

    public List<Token> tokenize(final CharacterStream tokenStream) {
        final List<Token> tokens = new ArrayList<>();

        while (tokenStream.hasNext()) {
            final char token = Character.toLowerCase(tokenStream.peek());

            if (token == '(') {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.LEFT_PAREN));
            } else if (token == ')') {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.RIGHT_PAREN));
            } else if (token == '{') {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.LEFT_FLOWER_PAREN));
            } else if (token == '}') {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.RIGHT_FLOWER_PAREN));
            } else if (token == '[') {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.LEFT_SQUARE_PAREN));
            } else if (token == ']') {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.RIGHT_SQUARE_PAREN));
            }/*
            else if (Character.isDigit(token) || token == '.' || token == '-') {
                tokens.add(numberTokenizer.tokenize(tokenStream));
            }*/ else if (ART_LOG_FUNC.contains(token + "")) {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.ARTHEMATICFUNCTION));
            } else if (Character.isAlphabetic(tokenStream.peek()) ||
                    SPECIAL_CHAR.contains(tokenStream.peek() + "") || Character.isDigit(token)) {
                tokens.add(consumeString(tokenStream));
            } else if (token == ',' || token == '\t' || token == ' ') {
                tokenStream.next(); // ignore token
            }/*
            else if((Arrays.asList(">","<" , "=","!")).contains(token+"")) {
            	tokens.add(logicalOperatorsTokenizer.tokenize(tokenStream));
            }*/ else if (token == ',') {
                tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.COMMA));
            }/*else if (ART_FUNC.contains(token+"")) {
            	 tokens.add(new Token(String.valueOf(tokenStream.next()), TokenType.ARTHEMATICFUNCTION));
            }*/ else {
                throw new LexerException("Found unrecognized character [" + token + "]");
            }
        }

        return tokens;
    }


    private Token consumeString(final CharacterStream tokenStream) {
        final StringBuilder stringBuilder = new StringBuilder();

        while (tokenStream.hasNext()) {
            if (Character.isAlphabetic(tokenStream.peek()) ||
                    SPECIAL_CHAR.contains(tokenStream.peek() + "") ||
                    Character.isDigit(tokenStream.peek()) || ' ' == (tokenStream.peek())) {
                stringBuilder.append(tokenStream.next());

            } else {
                break;
            }
        }
        final String token = stringBuilder.toString();

        if (FUNCTIONS.contains(token.toUpperCase().trim())) {
            return new Token(token.trim(), TokenType.FUNCTION);
        } else if (BOOLEAN.contains(token)) {
            return new Token(token, TokenType.VARIABLE);
        }
        return new Token(token, TokenType.VARIABLE);
    }


}
