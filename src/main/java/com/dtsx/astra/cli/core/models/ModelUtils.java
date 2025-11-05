package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Map;
import java.util.StringJoiner;

@UtilityClass
public class ModelUtils {
    public static String trim(@NonNull String value) {
        return StringUtils.removeQuotesIfAny(value.trim());
    }

    public static Either<String, String> validateBasics(String thing, @NonNull String value) {
        return validateNotBlank(thing, value)
            .flatMap(v -> validateNotPlaceholder(thing, v))
            .flatMap(v -> validateDoesntHaveInvalidChars(thing, v));
    }

    public static Either<String, String> trimAndValidateBasics(String thing, @NonNull String value) {
        return validateBasics(thing, trim(value));
    }

    private static Either<String, String> validateNotBlank(String thing, @NonNull String value) {
        if (value.isBlank()) {
            return Either.left(thing + " should not be blank or empty");
        }
        return Either.pure(value);
    }

    private static Either<String, String> validateNotPlaceholder(String thing, @NonNull String value) {
        if (value.startsWith("<") && value.endsWith(">")) {
            return Either.left(thing + " should not be enclosed in angle brackets... did you forget to replace a placeholder?");
        }
        return Either.pure(value);
    }

    private static Either<String, String> validateDoesntHaveInvalidChars(String thing, @NonNull String value) {
        val invalidChars = Map.of(
            "<newline>", '\n',
            "<carriage return>", '\r'
        );

        val offenders = new StringJoiner(",");

        for (val entry : invalidChars.entrySet()) {
            if (value.indexOf(entry.getValue()) >= 0) {
                offenders.add(entry.getKey());
            }
        }

        if (offenders.length() > 0) {
            return Either.left(thing + " contains the following invalid character(s): '" + offenders + "'");
        }

        return Either.pure(value);
    }
}
