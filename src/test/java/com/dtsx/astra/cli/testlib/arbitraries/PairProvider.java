package com.dtsx.astra.cli.testlib.arbitraries;

import lombok.val;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.graalvm.collections.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class PairProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Pair.class);
    }

    @Override
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        val typeA = targetType.getTypeArgument(0);
        val typeB = targetType.getTypeArgument(1);

        return subtypeProvider.apply(typeA).stream()
            .flatMap((arbA) ->
                subtypeProvider.apply(typeB).stream().map((arbB) ->
                    arbA.flatMap(a -> arbB.map(b -> Pair.create(a, b)))
                )
            )
            .collect(Collectors.toSet());
    }
}
