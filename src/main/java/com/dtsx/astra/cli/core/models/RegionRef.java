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

    public static Either<String, Optional<RegionRef>> parse(@NonNull DbRef dbRef, Optional<String> maybeName) {
        var name = Optional.<String>empty();

        if (dbRef.hasPreferredRegion()) {
            name = dbRef.preferredRegion();
        }

        if (maybeName.isPresent()) {
            name = maybeName;
        }

        if (name.isEmpty()) {
            return Either.pure(Optional.empty());
        }

        if (ModelUtils.trim(name.get()).isBlank()) {
            return Either.left("Region should not be blank or empty. Use one of the `${cli.name} db list-regions-*` commands to see available regions.");
        }

        return ModelUtils.trimAndValidateBasics("Region", name.get())
            .map(RegionRef::new)
            .map(Optional::of);
    }

    public static Optional<RegionRef> mustParse(@NonNull DbRef dbRef, @Nullable String name) {
        return parse(dbRef, Optional.ofNullable(name)).getRight((err -> {
            throw new OptionValidationException("region name", err);
        }));
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
