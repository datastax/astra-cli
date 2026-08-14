package com.dtsx.astra.cli.testlib.arbitraries;

import com.dtsx.astra.sdk.utils.AstraEnvironment;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class AstraEnvironmentProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(AstraEnvironment.class);
    }

    @Override
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        return Collections.singleton(
            Arbitraries.oneOf(
                Arbitraries.just(AstraEnvironment.PROD),
                Arbitraries.just(AstraEnvironment.DEV),
                Arbitraries.just(AstraEnvironment.TEST),
                Arbitraries.strings().map(AstraEnvironment::local)
            )
        );
    }
}
