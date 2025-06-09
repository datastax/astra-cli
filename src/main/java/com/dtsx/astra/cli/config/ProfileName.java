package com.dtsx.astra.cli.config;

import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.utils.Either;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.Value;

@Value
public class ProfileName implements AstraColors.Highlightable {
    public static final ProfileName DEFAULT = ProfileName.mkUnsafe("default");

    public static final String PARAM_LABEL = "PROFILE_NAME";

    String name;

    public static Either<String, ProfileName> parse(String name) {
        if (name.contains("\n") || name.contains("\r")) {
            return Either.left("Profile name cannot contain newlines or carriage returns.");
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

    @Override
    public String toString() {
        return unwrap();
    }

    @Override
    public String highlight() {
        return AstraColors.highlight(unwrap());
    }
}
