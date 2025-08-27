package com.dtsx.astra.cli.testlib.arbitraries;

import com.dtsx.astra.cli.core.models.AstraToken;
import lombok.val;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class AstraTokenProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(AstraToken.class);
    }

    @Override
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        val base = Arbitraries.strings().ascii();

        val arbs = Combinators.combine(
            base.ofLength(24),
            base.ofLength(64)
        );

        val arb = arbs.as((part2, part3) -> (
            AstraToken.mkUnsafe("AstraCS:" + part2 + ":" + part3)
        ));

        return Collections.singleton(arb);
    }
}
