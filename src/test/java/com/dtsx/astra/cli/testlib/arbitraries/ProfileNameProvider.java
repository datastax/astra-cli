package com.dtsx.astra.cli.testlib.arbitraries;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class ProfileNameProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(ProfileName.class);
    }

    @Override
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        return Collections.singleton(
            Arbitraries.strings().alpha().map(ProfileName::parse).filter(Either::isRight).map(Either::getRight)
        );
    }
}
