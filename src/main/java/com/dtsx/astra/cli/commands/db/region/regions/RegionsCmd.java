package com.dtsx.astra.cli.commands.db.region.regions;

import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "regions",
    description = "List available regions for Astra DB databases",
    subcommands = {
        RegionsClassicCmd.class,
        RegionsServerlessCmd.class,
        RegionsVectorCmd.class
    }
)
public class RegionsCmd {
    @Mixin
    public HelpMixin help;
}
