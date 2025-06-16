package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class RegionListOperation {
    private final RegionGateway regionGateway;
    private final DbGateway dbGateway;

    public record FoundRegions(
        Datacenter defaultRegion,
        List<Datacenter> regions
    ) {}

    public FoundRegions execute(DbRef dbRef) {
        val dcs = regionGateway.findRegionsForDb(dbRef);

        val defaultRegion = AstraLogger.loading("Fetching default region for db " + highlight(dbRef), (_) -> (
            dbGateway.findOneDb(dbRef).getInfo().getRegion()
        ));

        return new FoundRegions(
            dcs.stream()
                .filter(dc -> dc.getRegion().equalsIgnoreCase(defaultRegion))
                .findFirst()
                .orElseThrow(),
            dcs
        );
    }
}
