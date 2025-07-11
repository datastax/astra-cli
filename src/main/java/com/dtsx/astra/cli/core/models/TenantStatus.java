package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraColors.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantStatus implements Highlightable {
    String unwrap;

    public static TenantStatus mkUnsafe(String status) {
        return new TenantStatus(status.toLowerCase());
    }

    @Override
    public String highlight() {
        return switch (unwrap) {
            case "active" -> AstraColors.GREEN_500.useOrQuote(unwrap);
            case "error" -> AstraColors.RED_500.useOrQuote(unwrap);
            default -> AstraColors.NEUTRAL_300.useOrQuote(unwrap);
        };
    }

    @Override
    @JsonValue
    public String toString() {
        return unwrap;
    }
}
