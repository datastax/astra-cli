package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AstraToken implements AstraColors.Highlightable {
    String token;

    public static Either<String, AstraToken> parse(@NonNull String token) {
        return Utils.trimAndValidateBasics("Astra token", token).flatMap((t) -> {
            if (!t.startsWith("AstraCS:")) {
                return Either.left("Astra token should start with 'AstraCS:'");
            }

            if (t.length() != 97) {
                return Either.left("Astra token should be exactly 97 characters long; yours is " + t.length());
            }

            return Either.right(mkUnsafe(t));
        });
    }

    public static AstraToken mkUnsafe(@NonNull String token) {
        return new AstraToken(token);
    }

    @JsonValue
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
