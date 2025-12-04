package com.dtsx.astra.cli.testlib.arbitraries;

import lombok.SneakyThrows;
import lombok.val;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.CannotFindArbitraryException;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

public class DataclassProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        val clazz = targetType.getRawType();

        val hasNoArgConstructor = Arrays.stream(clazz.getDeclaredConstructors())
            .anyMatch(constructor -> constructor.getParameterCount() == 0);

        if (!hasNoArgConstructor) {
            return false;
        }

        for (val field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            try {
                Arbitraries.defaultFor(TypeUsage.forType(field.getGenericType()));
            } catch (CannotFindArbitraryException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    @SneakyThrows
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        val clazz = targetType.getRawType();

        val noArgConstructor = clazz.getDeclaredConstructor();
        noArgConstructor.setAccessible(true);

        val fields = Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .filter(field -> !Modifier.isFinal(field.getModifiers()))
            .toList();

        val fieldArbs = fields.stream()
            .map(field -> Arbitraries.defaultFor(TypeUsage.forType(field.getGenericType())))
            .toList();

        val combined = Combinators.combine(fieldArbs).as((values) -> {
            try {
                val instance = noArgConstructor.newInstance();

                for (int i = 0; i < fields.size(); i++) {
                    val field = fields.get(i);
                    field.setAccessible(true);
                    field.set(instance, values.get(i));
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of '" + clazz.getName() + "': " + e.getMessage(), e);
            }
        });

        return Set.of(combined);
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }
}
