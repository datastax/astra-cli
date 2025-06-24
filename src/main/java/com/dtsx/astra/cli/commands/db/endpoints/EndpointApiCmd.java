package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointApiOperation;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.operations.db.endpoints.EndpointApiOperation.EndpointApiRequest;

@Command(
    name = "get-endpoint-api"
)
public class EndpointApiCmd extends AbstractEndpointGetCmd {
    @Override
    protected Operation<String> mkOperation() {
        return new EndpointApiOperation(dbGateway, new EndpointApiRequest(dbRef, region, profile().env()));
    }
}
