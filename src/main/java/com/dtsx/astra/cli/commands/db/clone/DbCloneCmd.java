package com.dtsx.astra.cli.commands.db.clone;

import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "clone",
    description = "Manage database cloning and snapshots",
    subcommands = {
        DbCloneStartCmd.class,
        DbCloneStatusCmd.class,
        DbCloneListSnapshotsCmd.class,
    }
)
public class DbCloneCmd {
    @Mixin
    public HelpMixin help;
}
