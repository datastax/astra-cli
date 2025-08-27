package com.dtsx.astra.cli.testlib.arbitraries;

import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.SneakyThrows;
import lombok.val;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DatabaseProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Database.class) && targetType.getMetaInfo("recursiveCheckForDbProvider").isEmpty();
    }

    @Override
    @SneakyThrows
    public @NotNull Set<Arbitrary<?>> provideFor(@NotNull TypeUsage targetType, @NotNull SubtypeProvider subtypeProvider) {
        val arb = Arbitraries.defaultFor(TypeUsage.of(Database.class).withMetaInfo("recursiveCheckForDbProvider", true))
            .map(db -> (Database) db)
            .filter(db -> !db.getInfo().getDatacenters().isEmpty())
            .filter(db -> db.getInfo().getDatacenters().size() == db.getInfo().getDatacenters().stream().map(Datacenter::getRegion).distinct().count())
            .map((db) -> {
                db.getInfo().setRegion(db.getInfo().getDatacenters().stream().findFirst().orElseThrow().getRegion());
                return db;
            });

        return Set.of(arb);
    }
}
