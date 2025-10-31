package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static com.dtsx.astra.cli.utils.Collectionutils.sequencedMapOf;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRef implements Highlightable {
    private final Either<UUID, String> ref;

    public static Either<String, UserRef> parse(@NonNull String ref) {
        return ModelUtils.trimAndValidateBasics("User email/id", ref).flatMap(trimmed -> {
            try {
                return Either.pure(new UserRef(Either.left(UUID.fromString(trimmed))));
            } catch (IllegalArgumentException e) {
                return Either.pure(new UserRef(Either.pure(trimmed)));
            }
        });
    }

    public static UserRef fromEmailUnsafe(@NonNull String email) {
        return new UserRef(Either.pure(email));
    }

    public <T> T fold(Function<UUID, T> idMapper, Function<String, T> emailMapper) {
        return ref.fold(idMapper, emailMapper);
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return ref.fold(
            id -> sequencedMapOf("type", "id", "value", id.toString()),
            email -> sequencedMapOf("type", "email", "value", email)
        );
    }

    @Override
    @JsonValue
    public String toString() {
        return ref.fold(UUID::toString, email -> email);
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(ref.fold(UUID::toString, email -> email));
    }
}
