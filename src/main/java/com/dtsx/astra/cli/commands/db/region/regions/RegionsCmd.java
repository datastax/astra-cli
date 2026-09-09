package com.dtsx.astra.cli.commands.db.region.regions;

import com.dtsx.astra.cli.commands.user.AbstractCmd;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand.None;
import picocli.CommandLine.Command;

@Command(
    name = "regions",
    description = "List available regions for Astra DB databases",
    subcommands = {
        RegionsClassicCmd.class,
        RegionsServerlessCmd.class,
        RegionsVectorCmd.class
    }
)
@AliasForSubcommand(None.class)
public class RegionsCmd extends AbstractCmd {}
