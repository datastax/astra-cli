package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {
    public static String trim(@NonNull String value) {
        return StringUtils.removeQuotesIfAny(value.trim());
    }

    public static Either<String, String> validateNotBlank(String thing, @NonNull String value) {
        if (value.isBlank()) {
            return Either.left(thing + " should not be blank or empty");
        }
        return Either.pure(value);
    }

    public static Either<String, String> validateNotPlaceholder(String thing, @NonNull String value) {
        if (value.startsWith("<") && value.endsWith(">")) {
            return Either.left(thing + " should not be enclosed in angle brackets... did you forget to replace a placeholder?");
        }
        return Either.pure(value);
    }

    public static Either<String, String> validateBasics(String thing, @NonNull String value) {
        return validateNotBlank(thing, value).flatMap(v -> validateNotPlaceholder(thing, v));
    }

    public static Either<String, String> trimAndValidateBasics(String thing, @NonNull String value) {
        return validateBasics(thing, trim(value));
    }
}
