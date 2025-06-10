package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.utils.Either;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyspaceRef implements AstraColors.Highlightable {
    private final String name;
    private final DbRef dbRef;

    public static Either<String, KeyspaceRef> parse(@NonNull DbRef database, @NonNull String name) {
        if (name.isBlank()) {
            return Either.left("Keyspace name cannot be blank or empty");
        }

        return Either.right(new KeyspaceRef(name, database));
    }

    public String name() {
        return name;
    }

    public DbRef db() {
        return dbRef;
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(name);
    }
}
