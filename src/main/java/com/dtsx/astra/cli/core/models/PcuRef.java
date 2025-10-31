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
public class PcuRef implements Highlightable {
    private final Either<UUID, String> ref;

    public static Either<String, PcuRef> parse(@NonNull String ref) {
        return ModelUtils.trimAndValidateBasics("PCU group name/id", ref).flatMap((trimmed) -> {
            try {
                return Either.pure(new PcuRef(Either.left(UUID.fromString(trimmed))));
            } catch (IllegalArgumentException e) {
                return Either.pure(new PcuRef(Either.pure(trimmed)));
            }
        });
    }

    public static PcuRef fromTitleUnsafe(@NonNull String name) {
        return new PcuRef(Either.pure(name));
    }

    public static PcuRef fromId(@NonNull UUID id) {
        return new PcuRef(Either.left(id));
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
            id -> sequencedMapOf("type", "id", "value", id.toString()),
            name -> sequencedMapOf("type", "name", "value", name)
        );
    }

    @Override
    public String toString() {
        return ref.fold(UUID::toString, name -> name);
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(ref.fold(UUID::toString, name -> name));
    }
}
