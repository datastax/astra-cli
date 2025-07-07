package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.commands.streaming.pulsar.StreamingPulsarTokenCmd;
import picocli.CommandLine.Command;

@Command(
    name = "streaming",
    description = "Manage Astra streaming tenants",
    subcommands = {
        StreamingListCmd.class,
        StreamingCreateCmd.class,
        StreamingDeleteCmd.class,
        StreamingStatusCmd.class,
        StreamingPulsarTokenCmd.class,
        StreamingListRegionsCmd.class,
        StreamingListCloudsCmd.class,
        StreamingExistCmd.class,
    }
)
public class StreamingCmd extends StreamingListImpl {}
