package com.dtsx.astra.cli.commands.db.clone;

import com.dtsx.astra.cli.commands.user.AbstractCmd;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand.None;
import picocli.CommandLine.Command;

@Command(
    name = "clone",
    description = "Manage database cloning and snapshots",
    subcommands = {
        DbCloneStartCmd.class,
        DbCloneStatusCmd.class,
        DbCloneListSnapshotsCmd.class,
    }
)
@AliasForSubcommand(None.class)
public class DbCloneCmd extends AbstractCmd {}
