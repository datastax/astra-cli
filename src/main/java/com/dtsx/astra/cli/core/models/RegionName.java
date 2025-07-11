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
public class RegionName implements AstraColors.Highlightable {
    String name;

    public static Either<String, RegionName> parse(@NonNull String name) {
        return Utils.trimAndValidateBasics("Region", name).map(RegionName::mkUnsafe);
    }

    public static RegionName mkUnsafe(@NonNull String name) {
        return new RegionName(name);
    }

    @JsonValue
    public String unwrap() {
        return name;
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
