package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.TenantStatus;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingStatusOperation;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.streaming.StreamingStatusOperation.StreamingStatusRequest;

@Command(
    name = "status",
    description = "Get the current status of a streaming tenant."
)
@Example(
    comment = "Get the status of a streaming tenant",
    command = "${cli.name} streaming status my_tenant"
)
public class StreamingStatusCmd extends AbstractStreamingTenantSpecificCmd<TenantStatus> {
    @Override
    protected final OutputHuman executeHuman(Supplier<TenantStatus> result) {
        return OutputHuman.message("Tenant %s is %s".formatted(highlight($tenantName), highlight(result.get())));
    }

    @Override
    protected final OutputAll execute(Supplier<TenantStatus> result) {
        return OutputAll.serializeValue(result);
    }

    @Override
    protected Operation<TenantStatus> mkOperation() {
        return new StreamingStatusOperation(streamingGateway, new StreamingStatusRequest($tenantName));
    }
}
