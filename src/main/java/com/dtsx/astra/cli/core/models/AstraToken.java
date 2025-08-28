package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.dtsx.astra.cli.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.stream.Stream;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AstraToken implements Highlightable {
    String token;

    public static Either<String, AstraToken> parse(@NonNull String token) {
        return Utils.trimAndValidateBasics("Astra token", token).flatMap((t) -> {
            if (StringUtils.isValidJson(token)) {
                return Either.left("Astra token should not be passed as JSON; it should be a plain string");
            }

            if (Stream.of(".txt", ".json", ".yaml", ".yml", ".env", ".csv").anyMatch(token::endsWith)) {
                return Either.left("Astra token looks like a file name; please use the @file syntax to pass the token from a file, where the file contains only the token as a plain string (e.g. `--token @token.txt`)");
            }

            if (!t.startsWith("AstraCS:")) {
                return Either.left("Astra token should start with 'AstraCS:'");
            }

            if (t.length() != 97) {
                return Either.left("Astra token should be exactly 97 characters long; yours is " + t.length());
            }

            val split = t.split(":");

            if (split.length != 3) {
                return Either.left("Astra token should contain exactly two parts separated by ':'");
            }

            if (split[1].length() != 24) {
                return Either.left("The second part of the Astra token should be exactly 24 characters long; yours is " + split[1].length());
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
    public String highlight(CliContext ctx) {
        return ctx.highlight(token);
    }

    @Override
    public String toString() {
        return token.substring(0, "AstraCS:".length() + 4) + "****" + token.substring(token.length() - 4);
    }
}
