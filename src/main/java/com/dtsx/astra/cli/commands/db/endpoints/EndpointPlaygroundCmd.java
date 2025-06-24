package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointPlaygroundOperation;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.operations.db.endpoints.EndpointPlaygroundOperation.EndpointPlaygroundRequest;

@Command(
    name = "get-endpoint-playground"
)
public class EndpointPlaygroundCmd extends AbstractEndpointGetCmd {
    @Override
    protected Operation<String> mkOperation() {
        return new EndpointPlaygroundOperation(dbGateway, new EndpointPlaygroundRequest(dbRef, region, profile().env()));
    }
}
