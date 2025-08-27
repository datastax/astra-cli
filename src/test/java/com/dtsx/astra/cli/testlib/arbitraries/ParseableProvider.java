package com.dtsx.astra.cli.testlib.arbitraries;

import com.dtsx.astra.cli.core.datatypes.Either;
import lombok.val;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class ParseableProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(@NotNull TypeUsage targetType) {
        return findParseMethod(targetType).isPresent();
    }

    @Override
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        val parseMethod = findParseMethod(targetType).orElseThrow();

        val paramArbs = Arrays.stream(parseMethod.getGenericParameterTypes())
            .map(paramType -> Arbitraries.defaultFor(TypeUsage.forType(paramType)))
            .toList();

        val combined = Combinators.combine(paramArbs)
            .as((values) -> {
                try {
                    return (Either<?, ?>) parseMethod.invoke(null, values.toArray());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke parse method: " + e.getMessage(), e);
                }
            })
            .filter(Either::isRight)
            .map(Either::getRight);

        return Set.of(combined);
    }

    @Override
    public int priority() {
        return -1;
    }

    private Optional<Method> findParseMethod(TypeUsage targetType) {
        return Arrays.stream(targetType.getRawType().getMethods())
            .filter(m -> m.getName().equals("parse"))
            .filter(m -> Modifier.isStatic(m.getModifiers()))
            .filter(m -> m.getReturnType().equals(Either.class))
            .findFirst();
    }
}
