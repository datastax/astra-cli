package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetRequest;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import picocli.CommandLine.Option;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractEndpointGetCmd extends AbstractPromptForDbCmd<EndpointGetResponse> {
    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use",
        paramLabel = $Regions.LABEL
    )
    protected Optional<RegionName> region;

    protected abstract String mkEndpoint(EndpointGetResponse result);

    @Override
    protected final OutputAll execute(Supplier<EndpointGetResponse> result) {
        if (isLegacy()) {
            ctx.log().warn("@!astra db %s!@ is deprecated".formatted(spec.commandLine().getCommandName()));
            ctx.log().warn("Use the new command @!astra db get-endpoint %s!@ instead".formatted(spec.commandLine().getCommandName().replace("get-endpoint-", "")));
        }
        return OutputAll.serializeValue(mkEndpoint(result.get()));
    }

    @Override
    protected final Operation<EndpointGetResponse> mkOperation() {
        return new EndpointGetOperation(dbGateway, new EndpointGetRequest($dbRef, region, profile().env()));
    }

    @Override
    protected final String dbRefPrompt() {
        return "Select the database to get the endpoint for";
    }

    protected boolean isLegacy() {
        return false;
    }
}
