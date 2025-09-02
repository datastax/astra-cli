package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantStatus implements Highlightable {
    private final String unwrap;

    public static TenantStatus mkUnsafe(String status) {
        return new TenantStatus(status.toLowerCase());
    }

    public String unwrap() {
        return unwrap;
    }

    @Override
    public String highlight(CliContext ctx) {
        return switch (unwrap) {
            case "active" -> ctx.colors().GREEN_500.useOrQuote(unwrap);
            case "error" -> ctx.colors().RED_500.useOrQuote(unwrap);
            default -> ctx.colors().NEUTRAL_300.useOrQuote(unwrap);
        };
    }

    @Override
    @JsonValue
    public String toString() {
        return unwrap;
    }
}
