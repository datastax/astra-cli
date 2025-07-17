package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetRequest;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import picocli.CommandLine.Option;

import java.util.Optional;

public abstract class AbstractEndpointGetCmd extends AbstractPromptForDbCmd<EndpointGetResponse> {
    @Option(
        names = { "--region", "-r" },
        description = { "The region to use" },
        paramLabel = "REGION"
    )
    protected Optional<RegionName> region;

    protected abstract String mkEndpoint(EndpointGetResponse result);

    @Override
    protected final OutputAll execute(EndpointGetResponse result) {
        return OutputAll.serializeValue(mkEndpoint(result));
    }

    @Override
    protected Operation<EndpointGetResponse> mkOperation() {
        return new EndpointGetOperation(dbGateway, new EndpointGetRequest($dbRef, region, profile().env()));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to get the endpoint for";
    }
}
