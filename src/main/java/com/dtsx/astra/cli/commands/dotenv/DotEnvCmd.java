package com.dtsx.astra.cli.commands.dotenv;

import com.dtsx.astra.cli.commands.user.AbstractCmd;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand.None;
import picocli.CommandLine.Command;

@Command(
    name = "dotenv",
    description = "Easily create and update your .env files @|italic (beta)|@",
    subcommands = {
        DotEnvWriteCmd.class,
        DotEnvPrintCmd.class,
        DotEnvListKeysCmd.class,
    }
)
@AliasForSubcommand(None.class)
public class DotEnvCmd extends AbstractCmd {}
