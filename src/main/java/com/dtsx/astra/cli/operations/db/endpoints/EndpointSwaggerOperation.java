package com.dtsx.astra.cli.operations.db.endpoints;

import com.dtsx.astra.cli.core.exceptions.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

import static com.dtsx.astra.cli.operations.db.endpoints.EndpointSwaggerOperation.*;

@RequiredArgsConstructor
public class EndpointSwaggerOperation implements Operation<String> {
    private final DbGateway dbGateway;
    private final EndpointSwaggerRequest request;

    public record EndpointSwaggerRequest(
        DbRef dbRef,
        Optional<RegionName> region,
        AstraEnvironment env
    ) {}

    @Override
    public String execute() {
        val db = dbGateway.findOneDb(request.dbRef);

        if (request.region.isPresent()) {
            val regionIsValid = db.getInfo().getDatacenters().stream()
                .anyMatch(dc -> dc.getRegion().equalsIgnoreCase(request.region.get().unwrap()));

            if (!regionIsValid) {
                throw new RegionNotFoundException(request.dbRef, request.region.get());
            }
        }

        val effectiveRegion = request.region.orElse(RegionName.mkUnsafe(db.getInfo().getRegion()));
        
        return (db.getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(request.env, db.getId(), effectiveRegion.unwrap()) + "/swagger-ui/"
            : ApiLocator.getApiEndpoint(request.env, db.getId(), effectiveRegion.unwrap()) + "/api/json/swagger-ui/";
    }
}