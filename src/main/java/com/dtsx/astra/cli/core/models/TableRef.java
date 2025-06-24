package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TableRef implements AstraColors.Highlightable {
    private final String name;
    private final KeyspaceRef ksRef;

    public static Either<String, TableRef> parse(@NonNull KeyspaceRef keyspace, @NonNull String name) {
        if (name.isBlank()) {
            return Either.left("Table name cannot be blank or empty");
        }

        return Either.right(new TableRef(name, keyspace));
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
        return AstraColors.highlight(toString());
    }

    @Override
    public String toString() {
        return ksRef + "." + name;
    }
}
