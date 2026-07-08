package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RegionRef implements Highlightable {
    private final String name;

    public static Either<String, Optional<RegionRef>> parse(@Nullable DbRef dbRef, Optional<String> maybeName) {
        if (maybeName.isPresent()) {
            return parse(maybeName.get()).map(Optional::of);
        }

        if (dbRef != null) {
            return Either.pure(dbRef.preferredRegion());
        }

        return Either.pure(Optional.empty());
    }

    // Intentionally not registered as a type converter to prevent forgetting
    // that you should use the DbRef version of this method whenever possible
    public static Either<String, RegionRef> parse(String name) {
        if (ModelUtils.trim(name).isBlank()) {
            return Either.left("Region should not be blank or empty. Use one of the `${cli.name} db list-regions-*` commands to see available regions.");
        }

        return ModelUtils.trimAndValidateBasics("Region", name)
            .map(RegionRef::mkUnsafe);
    }

    public static Optional<RegionRef> mustParse(@Nullable DbRef dbRef, Optional<String> name) {
        return parse(dbRef, name).getRight((err -> {
            throw new OptionValidationException("region name", err);
        }));
    }

    public static RegionRef mustParse(String name) {
        return parse(name).getRight((err -> {
            throw new OptionValidationException("region name", err);
        }));
    }

    public static RegionRef mkUnsafe(@NonNull String name) {
        return new RegionRef(name);
    }

    @JsonValue
    public String unwrap() {
        return name;
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
