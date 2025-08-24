package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.region.RegionListOperation.*;

@RequiredArgsConstructor
public class RegionListOperation implements Operation<FoundRegions> {
    private final RegionGateway regionGateway;
    private final DbGateway dbGateway;
    private final RegionListRequest request;

    public record FoundRegions(
        Datacenter defaultRegion,
        List<Datacenter> regions
    ) {}

    public record RegionListRequest(DbRef dbRef) {}

    @Override
    public FoundRegions execute() {
        val dcs = regionGateway.findAllForDb(request.dbRef);

        val defaultRegion = AstraLogger.loading("Fetching default region for db " + highlight(request.dbRef), (_) -> (
            dbGateway.findOne(request.dbRef).getInfo().getRegion()
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
