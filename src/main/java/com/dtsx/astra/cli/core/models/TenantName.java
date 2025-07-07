package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantName implements AstraColors.Highlightable {
    String name;

    public static Either<String, TenantName> parse(@NonNull String name) {
        if (name.isBlank()) {
            return Either.left("Tenant name cannot be blank or empty");
        }

        return Either.right(TenantName.mkUnsafe(name));
    }

    public static TenantName mkUnsafe(@NonNull String name) {
        return new TenantName(name);
    }

    @JsonValue
    public String unwrap() {
        return name;
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(toString());
    }

    @Override
    public String toString() {
        return name;
    }
}
