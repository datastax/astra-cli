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
public class DbRef implements AstraColors.Highlightable {
    private final Either<UUID, String> ref;

    public static Either<String, DbRef> parse(@NonNull String ref) {
        return Utils.trimAndValidateBasics("Database name/id", ref).flatMap((trimmed) -> {
            try {
                return Either.right(new DbRef(Either.left(UUID.fromString(trimmed))));
            } catch (IllegalArgumentException e) {
                return Either.right(new DbRef(Either.right(trimmed)));
            }
        });
    }

    public static DbRef fromNameUnsafe(@NonNull String name) {
        return new DbRef(Either.right(name));
    }

    public static DbRef fromId(@NonNull UUID id) {
        return new DbRef(Either.left(id));
    }

    public boolean isId() {
        return ref.isLeft();
    }

    public boolean isName() {
        return ref.isRight();
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
    public String toString() {
        return ref.fold(UUID::toString, name -> name);
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(ref.fold(UUID::toString, name -> name));
    }
}
