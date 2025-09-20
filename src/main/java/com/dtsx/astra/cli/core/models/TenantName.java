package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantName implements Highlightable {
    private final String name;

    public static Either<String, TenantName> parse(@NonNull String name) {
        return ModelUtils.trimAndValidateBasics("Tenant name", name).map(TenantName::mkUnsafe);
    }

    public static TenantName mkUnsafe(@NonNull String name) {
        return new TenantName(name);
    }

    @JsonValue
    public String unwrap() {
        return name;
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(toString());
    }

    @Override
    public String toString() {
        return name;
    }
}
