package com.dtsx.astra.cli.gateways.db.region;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import lombok.RequiredArgsConstructor;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class RegionGatewayImpl implements RegionGateway {
    private final APIProvider api;

    @Override
    public void deleteRegion(DbRef ref, String region) {
        AstraLogger.loading("Deleting region " + highlight(region) + " for db " + highlight(ref), (_) -> {
            api.dbOpsClient(ref).datacenters().delete(region);
            return null;
        });
    }
}
