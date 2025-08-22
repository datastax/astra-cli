package com.dtsx.astra.cli.commands.db.dsbulk;

import picocli.CommandLine.Command;

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
public class DbDsbulkCmd {}
