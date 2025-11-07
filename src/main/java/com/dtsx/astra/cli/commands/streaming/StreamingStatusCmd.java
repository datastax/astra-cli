package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.commands.streaming.pulsar.AbstractStreamingPromptForTenantCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.TenantStatus;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingStatusOperation;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.streaming.StreamingStatusOperation.StreamingStatusRequest;

@Command(
    name = "status",
    description = "Get the current status of a streaming tenant."
)
@Example(
    comment = "Get the status of a streaming tenant",
    command = "${cli.name} streaming status my_tenant"
)
public class StreamingStatusCmd extends AbstractStreamingPromptForTenantCmd<TenantStatus> {
    @Override
    protected final OutputHuman executeHuman(Supplier<TenantStatus> result) {
        return OutputHuman.response("Tenant %s is %s".formatted(ctx.highlight($tenantName), ctx.highlight(result.get())));
    }

    @Override
    protected final OutputAll execute(Supplier<TenantStatus> result) {
        return OutputAll.serializeValue(result.get());
    }

    @Override
    protected Operation<TenantStatus> mkOperation() {
        return new StreamingStatusOperation(ctx, streamingGateway, new StreamingStatusRequest($tenantName));
    }

    @Override
    protected String tenantNamePrompt() {
        return "Select the streaming tenant to get the status for:";
    }
}
