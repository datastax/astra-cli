package com.dtsx.astra.cli.commands.streaming.pulsar;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "pulsar",
    description = "Apache Pulsar operations for your Astra streaming tenants",
    subcommands = {
        StreamingPulsarShellCmd.class,
        StreamingPulsarVersionCmd.class,
        StreamingPulsarPathCmd.class,
    }
)
@Example(
    comment = "Launch pulsar shell for a tenant",
    command = "${cli.name} streaming pulsar shell my_tenant"
)
@Example(
    comment = "Get pulsar version",
    command = "${cli.name} streaming pulsar version"
)
@Example(
    comment = "Get pulsar executable path",
    command = "${cli.name} streaming pulsar path"
)
public class StreamingPulsarCmd {}
