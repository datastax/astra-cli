package com.dtsx.astra.cli.testlib.arbitraries;

import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class ProfileProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Profile.class);
    }

    @Override
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        val arbs = Combinators.combine(
            Arbitraries.oneOf(Arbitraries.just(ProfileName.DEFAULT).optional(.5), Arbitraries.defaultFor(ProfileName.class).optional(1)), // higher chance of DEFAULT
            Arbitraries.defaultFor(AstraToken.class),
            Arbitraries.defaultFor(AstraEnvironment.class),
            Arbitraries.defaultFor(ProfileName.class).optional(.8)
        );

        val arb = arbs.as((name, token, env, source) -> {
            return new Profile(name, token, env, source.filter(s -> name.isPresent() && name.get().isDefault() && !s.equals(ProfileName.DEFAULT))); // satisfies profile source invariants
        });

        return Collections.singleton(arb);
    }
}
