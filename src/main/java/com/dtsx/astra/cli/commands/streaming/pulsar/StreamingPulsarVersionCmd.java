package com.dtsx.astra.cli.commands.streaming.pulsar;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarVersionOperation;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarVersionOperation.PulsarVersionRequest;
import picocli.CommandLine.Command;

@Command(
    name = "version",
    description = "Display the currently installed pulsar's version"
)
@Example(
    comment = "Display pulsar's version information",
    command = "${cli.name} streaming pulsar version"
)
public class StreamingPulsarVersionCmd extends AbstractPulsarExecCmd {
    @Override
    protected StreamingPulsarVersionOperation mkOperation() {
        return new StreamingPulsarVersionOperation(streamingGateway, downloadsGateway, new PulsarVersionRequest());
    }
}
