package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RegionName implements Highlightable {
    private final String name;

    public static Either<String, RegionName> parse(@NonNull String name) {
        if (ModelUtils.trim(name).isBlank()) {
            return Either.left("Region should not be blank or empty. Use one of the `${cli.name} db list-regions-*` commands to see available regions.");
        }
        return ModelUtils.trimAndValidateBasics("Region", name).map(RegionName::mkUnsafe);
    }

    public static RegionName mkUnsafe(@NonNull String name) {
        return new RegionName(name);
    }

    @JsonValue
    public String unwrap() {
        return name;
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
