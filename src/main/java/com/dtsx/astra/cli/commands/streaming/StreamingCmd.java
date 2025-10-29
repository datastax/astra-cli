package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.commands.streaming.pulsar.StreamingPulsarCmd;
import com.dtsx.astra.cli.commands.streaming.pulsar.StreamingPulsarTokenCmd;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import picocli.CommandLine.Command;

@Command(
    name = "streaming",
    description = "Manage Astra streaming tenants",
    subcommands = {
        StreamingListCmd.class,
        StreamingGetCmd.class,
        StreamingCreateCmd.class,
        StreamingDeleteCmd.class,
        StreamingExistCmd.class,
        StreamingStatusCmd.class,
        StreamingPulsarCmd.class,
        StreamingPulsarTokenCmd.class,
        StreamingListRegionsCmd.class,
        StreamingListCloudsCmd.class,
    }
)
@AliasForSubcommand(StreamingListCmd.class)
public class StreamingCmd extends StreamingListImpl {}
