package com.dtsx.astra.cli.commands.db.endpoints.legacy;

import com.dtsx.astra.cli.commands.db.endpoints.AbstractEndpointGetCmd;
import com.dtsx.astra.cli.commands.db.endpoints.Endpoint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

@Command(
    hidden = true
)
public abstract class AbstractLegacyEndpointGetCmd extends AbstractEndpointGetCmd {
    public AbstractLegacyEndpointGetCmd(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    protected final OutputAll execute(Supplier<EndpointGetResponse> result) {
        ctx.log().warn("@'!astra db %s!@ is deprecated".formatted(spec.commandLine().getCommandName()));
        ctx.log().warn("Use the new command @'!astra db endpoints %s!@ instead".formatted(spec.commandLine().getCommandName().replace("endpoints-", "")));
        return super.execute(result);
    }
}
