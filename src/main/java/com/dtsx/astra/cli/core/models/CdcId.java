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
public class CdcId implements AstraColors.Highlightable {
    String id;

    public static Either<String, CdcId> parse(@NonNull String id) {
        return Utils.trimAndValidateBasics("Cdc ID", id).map(CdcId::new);
    }

    @JsonValue
    public String unwrap() {
        return id;
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(toString());
    }

    @Override
    public String toString() {
        return id;
    }
}
