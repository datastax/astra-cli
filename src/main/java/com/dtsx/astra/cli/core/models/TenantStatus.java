package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraColors.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantStatus implements Highlightable {
    String status;

    public static Either<String, TenantStatus> parse(@NonNull String name) {
        if (name.isBlank()) {
            return Either.left("Tenant status cannot be blank or empty");
        }

        return Either.right(TenantStatus.mkUnsafe(name));
    }

    public static TenantStatus mkUnsafe(String status) {
        return new TenantStatus(status.toLowerCase());
    }

    @Override
    public String highlight() {
        return switch (status) {
            case "active" -> AstraColors.GREEN_500.useOrQuote(status);
            case "error" -> AstraColors.RED_500.useOrQuote(status);
            default -> AstraColors.NEUTRAL_300.useOrQuote(status);
        };
    }

    @Override
    @JsonValue
    public String toString() {
        return status;
    }
}
