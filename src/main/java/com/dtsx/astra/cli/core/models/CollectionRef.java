package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.utils.Either;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionRef implements AstraColors.Highlightable {
    private final String name;
    private final KeyspaceRef ksRef;

    public static Either<String, CollectionRef> parse(@NonNull KeyspaceRef keyspace, @NonNull String name) {
        if (name.isBlank()) {
            return Either.left("Collection name cannot be blank or empty");
        }

        return Either.right(new CollectionRef(name, keyspace));
    }

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
    public String highlight() {
        return AstraColors.highlight(ksRef + "." + name);
    }
}
