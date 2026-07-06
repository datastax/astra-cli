package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.core.CliConstants.$Copy;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.models.RegionRef;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetRequest;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import com.dtsx.astra.cli.utils.ShellUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import picocli.CommandLine.Option;

import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class AbstractEndpointGetCmd extends AbstractPromptForDbCmd<EndpointGetResponse> {
    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use",
        paramLabel = $Regions.LABEL
    )
    public Optional<String> $regionName;

    @Option(
        names = { $Copy.SHORT, $Copy.LONG },
        description = "Copy the endpoint to clipboard as well as print it"
    )
    boolean $copyToClipboard;

    private final Endpoint endpoint;

    @Override
    protected OutputAll execute(Supplier<EndpointGetResponse> res) {
        val out = endpoint.mkUrl(res.get(), profile().env());

        if ($copyToClipboard) {
            ShellUtils.copyToClipboard(ctx, out);
        }

        return OutputAll.serializeValue(out);
    }

    @Override
    protected final Operation<EndpointGetResponse> mkOperation() {
        val regionRef = RegionRef.mustParse($dbRef, $regionName);
        return new EndpointGetOperation(dbGateway, new EndpointGetRequest($dbRef, regionRef, profile().env()));
    }

    @Override
    protected final String dbRefPrompt() {
        return "Select the database to get the endpoint for";
    }
}
