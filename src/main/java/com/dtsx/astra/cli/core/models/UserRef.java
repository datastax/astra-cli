package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRef implements AstraColors.Highlightable {
    private final Either<UUID, String> ref;

    public static Either<String, UserRef> parse(@NonNull String ref) {
        return Utils.trimAndValidateBasics("User email/id", ref).flatMap(trimmed -> {
            try {
                return Either.right(new UserRef(Either.left(UUID.fromString(trimmed))));
            } catch (IllegalArgumentException e) {
                return Either.right(new UserRef(Either.right(trimmed)));
            }
        });
    }

    public <T> T fold(Function<UUID, T> idMapper, Function<String, T> emailMapper) {
        return ref.fold(idMapper, emailMapper);
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return ref.fold(
            id -> Map.of("type", "id", "unwrap", id.toString()),
            email -> Map.of("type", "email", "unwrap", email)
        );
    }

    @Override
    @JsonValue
    public String toString() {
        return ref.fold(UUID::toString, email -> email);
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(ref.fold(UUID::toString, email -> email));
    }
}
