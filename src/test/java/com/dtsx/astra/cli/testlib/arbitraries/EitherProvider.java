package com.dtsx.astra.cli.testlib.arbitraries;

import com.dtsx.astra.cli.core.datatypes.Either;
import lombok.val;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class EitherProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.canBeAssignedTo(TypeUsage.of(Either.class, TypeUsage.wildcard(TypeUsage.OBJECT_TYPE), TypeUsage.wildcard(TypeUsage.OBJECT_TYPE)));
    }

    @Override
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        val typeL = targetType.getTypeArgument(0);
        val typeR = targetType.getTypeArgument(1);

        return subtypeProvider.apply(typeL).stream()
            .flatMap((arbL) ->
                subtypeProvider.apply(typeR).stream().map((arbR) ->
                    Arbitraries.oneOf(arbL.map(Either::left), arbR.map(Either::pure))
                )
            )
            .collect(Collectors.toSet());
    }
}
