package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "dsbulk",
    description = "Use dsbulk to interface with your Astra DB database",
    subcommands = {
        DbDsbulkCountCmd.class,
        DbDsbulkLoadCmd.class,
        DbDsbulkUnloadCmd.class,
        DbDsbulkVersionCmd.class,
        DbDsbulkPathCmd.class,
    }
)
public class DbDsbulkCmd {
    @Mixin
    public HelpMixin help;
}
