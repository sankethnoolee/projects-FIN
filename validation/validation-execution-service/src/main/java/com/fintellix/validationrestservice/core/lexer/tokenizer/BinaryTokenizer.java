package com.fintellix.validationrestservice.core.lexer.tokenizer;

import com.fintellix.validationrestservice.core.lexer.token.NumberToken;
import com.fintellix.validationrestservice.core.lexer.token.NumberToken.Base;
import com.fintellix.validationrestservice.core.lexer.token.Token;

public class BinaryTokenizer {

    public Token tokenize(final CharacterStream tokenStream) {
        final StringBuilder stringBuilder = new StringBuilder();
        while (tokenStream.hasNext()) {
            switch (tokenStream.peek()) {
                case '0':
                case '1':
                    stringBuilder.append(tokenStream.next());
                    break;

                default:
                    return new NumberToken(stringBuilder.toString(), Base.BINARY);
            }
        }
        return new NumberToken(stringBuilder.toString(), Base.BINARY);
    }

}
