package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.exceptions.internal.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.PcuAssocTarget;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Comparator;
import java.util.Optional;

@UtilityClass
public class DbUtils {
    public static Datacenter resolveDatacenter(Database db, Optional<RegionName> maybeRegionName) {
        val regionName = maybeRegionName.orElse(RegionName.mkUnsafe(db.getInfo().getRegion()));

        return db.getInfo().getDatacenters().stream()
            .sorted(Comparator.comparing(Datacenter::getId))
            .filter(dc -> dc.getRegion().equalsIgnoreCase(regionName.unwrap()))
            .findFirst()
            .orElseThrow(() -> new RegionNotFoundException(DbRef.fromNameUnsafe(db.getInfo().getName()), regionName));
    }

    public static RegionName resolveRegionName(Database db, Optional<RegionName> maybeRegionName) {
        return RegionName.mkUnsafe(resolveDatacenter(db, maybeRegionName).getRegion());
    }

    public static DatacenterId resolvePcuAssocTarget(DbGateway dbGateway, PcuAssocTarget pcuAssocTarget) {
        return pcuAssocTarget.fold((dc) -> dc, (dbRef) -> {
            val db = dbGateway.findOne(dbRef);
            val dcs = db.getInfo().getDatacenters();

            if (dcs.size() != 1) {
                throw new OptionValidationException("pcu association target", "Database '%s' has %d regions (expected 1); please use a specific datacenter id instead".formatted(dbRef, dcs.size()));
            }

            val dc = dcs.iterator().next();

            return DatacenterId.parse(dc.getId()).getRight((e) -> new CongratsYouFoundABugException("Unexpected invalid datacenter id '%s' for database '%s' returned from the DevOps API: %s".formatted(dc.getId(), dbRef, e)));
        });
    }
}
