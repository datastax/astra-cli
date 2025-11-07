package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingExistOperation;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.streaming.StreamingExistOperation.StreamingExistRequest;

@Command(
    name = "exist",
    description = "Show existence of a streaming tenant",
    hidden = true
)
@Example(
    comment = "Check if a streaming tenant exists",
    command = "${cli.name} streaming exist my_tenant"
)
public class StreamingExistCmd extends AbstractStreamingTenantRequiredCmd<Boolean> {
    @Override
    protected final OutputHuman executeHuman(Supplier<Boolean> exists) {
        if (exists.get()) {
            return OutputHuman.response("Tenant %s exists.".formatted(ctx.highlight($tenantName)));
        } else {
            return OutputHuman.response("Tenant %s does not exist.".formatted(ctx.highlight($tenantName)));
        }
    }

    @Override
    protected final OutputAll execute(Supplier<Boolean> exists) {
        return OutputAll.serializeValue(exists.get());
    }

    @Override
    protected Operation<Boolean> mkOperation() {
        return new StreamingExistOperation(streamingGateway, new StreamingExistRequest($tenantName));
    }
}
