package com.dtsx.astra.cli.core.models;

import com.datastax.astra.internal.api.AstraApiEndpoint;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DbRef implements Highlightable {
    private final Either<UUID, String> ref;

    @Getter
    private final Optional<RegionRef> preferredRegion;

    public static Either<String, DbRef> parse(@NonNull String ref) {
        return ModelUtils.trimAndValidateBasics("Database name/id/endpoint", ref).flatMap((trimmed) -> {
            try {
                val endpoint = AstraApiEndpoint.parse(trimmed);

                val id = endpoint.getDatabaseId();
                val region = RegionRef.mkUnsafe(endpoint.getDatabaseRegion());

                return Either.pure(new DbRef(Either.left(id), Optional.of(region)));
            } catch (Exception e) {
                // not a valid Astra endpoint URL, fall through
            }

            try {
                return Either.pure(new DbRef(Either.left(UUID.fromString(trimmed)), Optional.empty()));
            } catch (IllegalArgumentException e) {
                return Either.pure(new DbRef(Either.pure(trimmed), Optional.empty()));
            }
        });
    }

    public static DbRef fromNameUnsafe(@NonNull String name) {
        return new DbRef(Either.pure(name), Optional.empty());
    }

    public static DbRef fromId(@NonNull UUID id) {
        return new DbRef(Either.left(id), Optional.empty());
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
        return ctx.highlight(toString());
    }
}
