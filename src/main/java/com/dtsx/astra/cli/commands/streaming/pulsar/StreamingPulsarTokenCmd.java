package com.dtsx.astra.cli.commands.streaming.pulsar;

import com.dtsx.astra.cli.commands.streaming.AbstractStreamingTenantSpecificCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarTokenOperation;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarTokenOperation.PulsarTokenRequest;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

@Command(
    name = "pulsar-token",
    description = "Get the Pulsar token for a streaming tenant"
)
@Example(
    comment = "Get the Pulsar token for a tenant",
    command = "${cli.name} streaming pulsar-token my_tenant"
)
public class StreamingPulsarTokenCmd extends AbstractStreamingTenantSpecificCmd<String> {
    @Override
    protected final OutputAll execute(Supplier<String> token) {
        return OutputAll.serializeValue(token);
    }

    @Override
    protected Operation<String> mkOperation() {
        return new StreamingPulsarTokenOperation(streamingGateway, new PulsarTokenRequest($tenantName));
    }
}
