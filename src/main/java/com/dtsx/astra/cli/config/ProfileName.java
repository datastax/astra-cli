package com.dtsx.astra.cli.config;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
public class ProfileName implements AstraColors.Highlightable {
    public static final ProfileName DEFAULT = ProfileName.mkUnsafe("default");

    String name;

    public static Either<String, ProfileName> parse(String name) {
        if (name.contains("\n")) {
            return Either.left("Profile name cannot contain newlines.");
        }

        if (name.isBlank()) {
            return Either.left("Profile name cannot be blank or empty.");
        }

        return Either.right(ProfileName.mkUnsafe(name));
    }

    public static ProfileName mkUnsafe(String name) {
        return new ProfileName(name);
    }

    private ProfileName(String name) {
        this.name = StringUtils.removeQuotesIfAny(name);
    }

    public String unwrap() {
        return name;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public String toString() {
        return unwrap();
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(unwrap());
    }
}
