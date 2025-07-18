package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingExistOperation;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.streaming.StreamingExistOperation.StreamingExistRequest;

@Command(
    name = "exist",
    description = "Show existence of a streaming tenant",
    hidden = true
)
@Example(
    comment = "Check if a streaming tenant exists",
    command = "astra streaming exist my_tenant"
)
public class StreamingExistCmd extends AbstractStreamingTenantSpecificCmd<Boolean> {
    @Override
    protected final OutputHuman executeHuman(Supplier<Boolean> exists) {
        if (exists.get()) {
            return OutputHuman.message("Tenant %s exists.".formatted(highlight($tenantName)));
        } else {
            return OutputHuman.message("Tenant %s does not exist.".formatted(highlight($tenantName)));
        }
    }

    @Override
    protected final OutputAll execute(Supplier<Boolean> exists) {
        return OutputAll.serializeValue(exists);
    }

    @Override
    protected Operation<Boolean> mkOperation() {
        return new StreamingExistOperation(streamingGateway, new StreamingExistRequest($tenantName));
    }
}
