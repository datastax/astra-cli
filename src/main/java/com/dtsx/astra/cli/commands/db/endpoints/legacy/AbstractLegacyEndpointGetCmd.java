package com.dtsx.astra.cli.commands.db.endpoints.legacy;

import com.dtsx.astra.cli.commands.db.endpoints.AbstractEndpointGetCmd;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.endpoints.EndpointGetOperation.EndpointGetResponse;

import java.util.function.Supplier;

public abstract class AbstractLegacyEndpointGetCmd extends AbstractEndpointGetCmd {
    @Override
    protected final OutputAll execute(Supplier<EndpointGetResponse> result) {
        ctx.log().warn("@!astra db %s!@ is deprecated".formatted(spec.commandLine().getCommandName()));
        ctx.log().warn("Use the new command @!astra db get-endpoint %s!@ instead".formatted(spec.commandLine().getCommandName().replace("get-endpoint-", "")));
        return super.execute(result);
    }
}
