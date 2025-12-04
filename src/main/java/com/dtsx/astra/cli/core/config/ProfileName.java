package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.ModelUtils;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileName implements Highlightable {
    public static final ProfileName DEFAULT = ProfileName.mkUnsafe("default");

    private final String name;

    public static @NotNull Either<String, ProfileName> parse(String name) {
        return ModelUtils.trimAndValidateBasics("Profile name", name).flatMap((trimmed) -> {
            return Either.pure(ProfileName.mkUnsafe(trimmed));
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
    public String highlight(CliContext ctx) {
        return ctx.highlight(unwrap());
    }
}
