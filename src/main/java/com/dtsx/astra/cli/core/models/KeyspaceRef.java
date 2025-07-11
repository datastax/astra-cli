package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyspaceRef implements AstraColors.Highlightable {
    private final String name;
    private final DbRef dbRef;

    public static Either<String, KeyspaceRef> parse(@NonNull DbRef dbRef, @NonNull String name) {
        return Utils.trimAndValidateBasics("Keyspace name", name)
            .map((trimmed) -> mkUnsafe(dbRef, trimmed));
    }

    public static KeyspaceRef mkUnsafe(@NonNull DbRef dbRef, @NonNull String name) {
        return new KeyspaceRef(name, dbRef);
    }

    @JsonValue
    public String name() {
        return name;
    }

    public DbRef db() {
        return dbRef;
    }

    public boolean isDefaultKeyspace() {
        return "default_keyspace".equals(name);
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
