package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CdcId implements Highlightable {
    private final String id;

    public static Either<String, CdcId> parse(@NonNull String id) {
        return ModelUtils.trimAndValidateBasics("Cdc ID", id).map(CdcId::new);
    }

    @JsonValue
    public String unwrap() {
        return id;
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(toString());
    }

    @Override
    public String toString() {
        return id;
    }
}
