package com.dtsx.astra.cli.testlib.arbitraries;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.val;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class EnumProvider implements ArbitraryProvider {
    private static final List<Class<? extends Enum<? extends Enum<?>>>> CLASSES = List.of(
        DatabaseStatusType.class,
        CloudProviderType.class
    );

    @Override
    public boolean canProvideFor(net.jqwik.api.providers.TypeUsage targetType) {
        return targetType.isOfType(Enum.class);
    }

    @Override
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        val arb = Arbitraries.of(CLASSES).flatMap(e -> Arbitraries.of(e.getEnumConstants()));
        return Set.of(arb);
    }
}
