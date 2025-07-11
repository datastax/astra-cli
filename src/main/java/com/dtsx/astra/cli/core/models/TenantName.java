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
        return Utils.trimAndValidateBasics("Tenant name", name).map(TenantName::mkUnsafe);
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
