package com.dtsx.astra.cli.config;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.Utils;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class ProfileName implements AstraColors.Highlightable {
    public static final ProfileName DEFAULT = ProfileName.mkUnsafe("default");

    String name;

    public static Either<String, ProfileName> parse(String name) {
        return Utils.trimAndValidateBasics("Profile name", name).flatMap(trimmed -> {
            if (trimmed.contains("\n")) {
                return Either.left("Profile name cannot contain newlines");
            }
            return Either.right(ProfileName.mkUnsafe(trimmed));
        });
    }

    public static ProfileName mkUnsafe(String name) {
        return new ProfileName(name);
    }

    public String unwrap() {
        return name;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    @JsonValue
    public String toString() {
        return unwrap();
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(unwrap());
    }
}
