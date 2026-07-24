package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CloneOperationId implements Highlightable {
    private final UUID id;

    public static Either<String, CloneOperationId> parse(@NonNull String id) {
        return ModelUtils.trimAndValidateBasics("Clone Operation ID", id).flatMap((trimmed) -> {
            try {
                val uuid = UUID.fromString(trimmed);
                return Either.pure(mkUnsafe(uuid));
            } catch (IllegalArgumentException e) {
                return Either.left("Clone Operation ID is not a valid UUID.");
            }
        });
    }

    public static CloneOperationId mkUnsafe(@NonNull UUID id) {
        return new CloneOperationId(id);
    }

    @JsonValue
    public UUID unwrap() {
        return id;
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(unwrap());
    }

    @Override
    public String toString() {
        return unwrap().toString();
    }
}
