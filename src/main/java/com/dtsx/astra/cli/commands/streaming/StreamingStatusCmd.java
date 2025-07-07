package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.TenantStatus;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingStatusOperation;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.streaming.StreamingStatusOperation.StreamingStatusRequest;

@Command(
    name = "status",
    description = "Get the current status of a streaming tenant."
)
@Example(
    comment = "Get the status of a streaming tenant",
    command = "astra streaming status my_tenant"
)
public class StreamingStatusCmd extends AbstractStreamingTenantSpecificCmd<TenantStatus> {
    @Override
    protected final OutputHuman executeHuman(TenantStatus result) {
        return OutputHuman.message("Tenant %s is %s".formatted(highlight($tenantName), highlight(result)));
    }

    @Override
    protected final OutputAll execute(TenantStatus result) {
        return OutputAll.serializeValue(result);
    }

    @Override
    protected Operation<TenantStatus> mkOperation() {
        return new StreamingStatusOperation(streamingGateway, new StreamingStatusRequest($tenantName));
    }
}
