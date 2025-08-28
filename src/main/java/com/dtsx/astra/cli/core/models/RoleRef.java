package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RoleRef implements Highlightable {
    private final Either<UUID, String> ref;

    public static Either<String, RoleRef> parse(@NonNull String ref) {
        return Utils.trimAndValidateBasics("Role name/id", ref).flatMap(trimmed -> {
            try {
                return Either.right(new RoleRef(Either.left(UUID.fromString(trimmed))));
            } catch (IllegalArgumentException e) {
                return Either.right(new RoleRef(Either.right(trimmed)));
            }
        });
    }

    public static RoleRef fromId(@NonNull UUID id) {
        return new RoleRef(Either.left(id));
    }

    public static RoleRef fromNameUnsafe(@NonNull String name) {
        return new RoleRef(Either.right(name));
    }

    public <T> T fold(Function<UUID, T> idMapper, Function<String, T> nameMapper) {
        return ref.fold(idMapper, nameMapper);
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return ref.fold(
            id -> Map.of("type", "id", "unwrap", id.toString()),
            name -> Map.of("type", "name", "unwrap", name)
        );
    }

    @Override
    @JsonValue
    public String toString() {
        return ref.fold(UUID::toString, name -> name);
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(ref.fold(UUID::toString, name -> name));
    }
}
