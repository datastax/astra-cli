package com.dtsx.astra.cli.commands.dotenv;

import com.dtsx.astra.cli.commands.user.AbstractCmd;
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
public class DotEnvCmd extends AbstractCmd {}
