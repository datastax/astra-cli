package com.dtsx.astra.cli.testlib.arbitraries;

import lombok.val;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class PathProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Path.class);
    }

    @Override
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        val arb = Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('_', '-', '.', '/')
            .ofMinLength(1)
            .ofMaxLength(20);

        return Collections.singleton(arb);
    }
}
