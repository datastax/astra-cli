package com.dtsx.astra.cli.operations.db.endpoints;

import com.dtsx.astra.cli.core.exceptions.internal.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

@RequiredArgsConstructor
public class EndpointGetOperation implements Operation<EndpointGetResponse> {
    private final DbGateway dbGateway;
    private final EndpointGetRequest request;

    public record EndpointGetRequest(
        DbRef dbRef,
        Optional<RegionName> region,
        AstraEnvironment env
    ) {}

    public record EndpointGetResponse(
        Database database,
        String region
    ) {}

    @Override
    public EndpointGetResponse execute() {
        val db = dbGateway.findOne(request.dbRef);

        if (request.region.isPresent()) {
            val regionIsValid = db.getInfo()
                .getDatacenters()
                .stream()
                .anyMatch((dc) -> dc.getRegion().equalsIgnoreCase(request.region.get().unwrap()));

            if (!regionIsValid) {
                throw new RegionNotFoundException(request.dbRef, request.region.get());
            }
        }

        val region = request.region.map(RegionName::unwrap).orElse(db.getInfo().getRegion());
        
        return new EndpointGetResponse(db, region);
    }
}
