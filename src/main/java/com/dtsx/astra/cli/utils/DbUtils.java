package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.exceptions.internal.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Optional;

@UtilityClass
public class DbUtils {
    public static Datacenter resolveDatacenter(Database db, Optional<RegionName> maybeRegionName) {
        val regionName = maybeRegionName.orElse(RegionName.mkUnsafe(db.getInfo().getRegion()));

        return db.getInfo().getDatacenters().stream()
            .filter(dc -> dc.getRegion().equalsIgnoreCase(regionName.unwrap()))
            .findFirst()
            .orElseThrow(() -> new RegionNotFoundException(DbRef.fromNameUnsafe(db.getInfo().getName()), regionName));
    }

    public static RegionName resolveRegionName(Database db, Optional<RegionName> maybeRegionName) {
        return RegionName.mkUnsafe(resolveDatacenter(db, maybeRegionName).getRegion());
    }
}
