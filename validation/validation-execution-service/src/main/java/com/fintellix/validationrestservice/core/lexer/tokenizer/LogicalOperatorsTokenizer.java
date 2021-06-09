package com.fintellix.validationrestservice.core.lexer.tokenizer;

import com.fintellix.validationrestservice.core.lexer.token.Token;
import com.fintellix.validationrestservice.core.lexer.token.TokenType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class LogicalOperatorsTokenizer {
    private static final Set<String> lOGICALOPS = new HashSet<>(Arrays.asList("==", ">", ">=", "<", "<=", "!="));

    public Token tokenize(final CharacterStream tokenStream) {
        final StringBuilder stringBuilder = new StringBuilder();

        while (tokenStream.hasNext()) {
            if ((Arrays.asList(">", "<", "=", "!")).contains(tokenStream.peek() + "")) {
                stringBuilder.append(tokenStream.next());

            } else {
                break;
            }
        }
        final String token = stringBuilder.toString();

        if (lOGICALOPS.contains(token)) {
            return new Token(token, TokenType.VARIABLE);
        }
        return new Token(token, TokenType.VARIABLE);
    }

}
