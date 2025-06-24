package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointSwaggerOperation;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.operations.db.endpoints.EndpointSwaggerOperation.EndpointSwaggerRequest;

@Command(
    name = "get-endpoint-swagger"
)
public class EndpointSwaggerCmd extends AbstractEndpointGetCmd {
    @Override
    protected Operation<String> mkOperation() {
        return new EndpointSwaggerOperation(dbGateway, new EndpointSwaggerRequest(dbRef, region, profile().env()));
    }
}
