package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionRef implements Highlightable {
    private final String name;
    private final KeyspaceRef ksRef;

    public static Either<String, CollectionRef> parse(@NonNull KeyspaceRef ksRef, @NonNull String name) {
        return ModelUtils.trimAndValidateBasics("Collection name", name)
            .map((trimmed) -> new CollectionRef(trimmed, ksRef));
    }

    public static CollectionRef mustParse(@NonNull KeyspaceRef ksRef, @NonNull String name) {
        return parse(ksRef, name).getRight((err) -> {
            throw new OptionValidationException("collection", err);
        });
    }

    public static CollectionRef mkUnsafe(@NonNull KeyspaceRef ksRef, @NonNull String name) {
        return new CollectionRef(name, ksRef);
    }

    @JsonValue
    public String name() {
        return name;
    }

    public KeyspaceRef keyspace() {
        return ksRef;
    }

    public DbRef db() {
        return ksRef.db();
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(toString());
    }

    @Override
    public String toString() {
        return ksRef + "." + name;
    }
}
