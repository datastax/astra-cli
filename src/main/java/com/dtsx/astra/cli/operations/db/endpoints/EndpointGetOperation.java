package com.dtsx.astra.cli.operations.db.endpoints;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import com.dtsx.astra.cli.utils.DbUtils;
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
        return new EndpointGetResponse(db, DbUtils.resolveRegionName(db, request.region).unwrap());
    }
}
