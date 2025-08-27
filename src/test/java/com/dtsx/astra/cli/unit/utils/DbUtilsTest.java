package com.dtsx.astra.cli.unit.utils;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.internal.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.utils.DbUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.SneakyThrows;
import lombok.val;
import net.jqwik.api.*;
import org.graalvm.collections.Pair;

import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Group
class DbUtilsTest {
    @Group
    class resolveDatacenter {
        @Property
        public void returns_default_datacenter_when_no_region_provided(@ForAll Database db) {
            val expected = findDatacenter(db, db.getInfo().getRegion());

            val result = DbUtils.resolveDatacenter(db, Optional.empty());

            assertThat(result).isEqualTo(expected);
        }

        @Property
        public void returns_corresponding_datacenter_given_valid_region(@ForAll("withSomeRegion") Pair<Database, RegionName> p) {
            val expected = findDatacenter(p.getLeft(), p.getRight().unwrap());

            val result = DbUtils.resolveDatacenter(p.getLeft(), Optional.of(p.getRight()));

            assertThat(result).isEqualTo(expected);
        }

        @Property
        public void throws_exception_when_region_not_found(@ForAll Database db, @ForAll RegionName regionName) {
            Assume.that(db.getInfo().getDatacenters().stream().noneMatch(dc -> dc.getRegion().equals(regionName.unwrap())));

            assertThatExceptionOfType(RegionNotFoundException.class)
                .isThrownBy(() -> DbUtils.resolveDatacenter(db, Optional.of(regionName)));
        }

        @Provide
        private Arbitrary<Pair<Database, RegionName>> withSomeRegion() {
            return Arbitraries.defaultFor(Database.class).map((db) -> {
                return Pair.create(db, RegionName.mkUnsafe(db.getInfo().getDatacenters().stream().findFirst().orElseThrow().getRegion()));
            });
        }

        @SneakyThrows
        private Datacenter findDatacenter(Database db, String region) {
            return db.getInfo().getDatacenters().stream().filter(dc -> dc.getRegion().equals(region)).findFirst().orElseThrow();
        }
    }

    @Group
    class resolveRegionName {
        @Property
        public void behaves_as_resolveDatacenter(@ForAll Database db, @ForAll RegionName regionName) {
            val expected = Either.tryCatch(
                () -> RegionName.mkUnsafe(DbUtils.resolveDatacenter(db, Optional.of(regionName)).getRegion()),
                Function.identity()
            );

            val result = Either.tryCatch(
                () -> DbUtils.resolveRegionName(db, Optional.of(regionName)),
                Function.identity()
            );

            assertEquals(result.bimap(Object::toString, Object::toString), expected.bimap(Object::toString, Object::toString));
        }
    }
}
