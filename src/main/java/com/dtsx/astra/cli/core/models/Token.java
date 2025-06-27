package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Token implements AstraColors.Highlightable {
    String token;

    public static Either<String, Token> parse(@NonNull String token) {
        if (token.isBlank()) {
            return Either.left("Astra token cannot be blank or empty");
        }

        token = StringUtils.removeQuotesIfAny(token.trim());

        if (!token.startsWith("AstraCS:")) {
            return Either.left("Astra token should start with 'AstraCS:'");
        }

        if (token.length() != 97) {
            return Either.left("Astra token should be exactly 98 characters long");
        }

        return Either.right(mkUnsafe(token));
    }

    public static Token mkUnsafe(@NonNull String token) {
        return new Token(token);
    }

    public String unwrap() {
        return token;
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(token);
    }

    @Override
    public String toString() {
        return token.substring(0, "AstraCS:".length() + 4) + "****" + token.substring(token.length() - 4);
    }
}
